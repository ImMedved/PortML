from __future__ import annotations

import datetime as dt
import logging
from bisect import insort
from collections import defaultdict
from datetime import timedelta, time
from typing import Dict, List, Tuple

from app.models.schemas import (
    PlanRequestModel, ShipModel, TerminalModel, AssignmentModel, PlanResponseModel,
    MetricsModel, PlanningAlgorithm
)

log = logging.getLogger(__name__)

# ────────────────────────────── helpers ─────────────────────────────
def overlap(a0: dt.datetime, a1: dt.datetime,
            b0: dt.datetime, b1: dt.datetime) -> bool:
    """True if intervals [a0,a1) and [b0,b1) intersect."""
    return a0 < b1 and b0 < a1


def merge(intervals: List[Tuple[dt.datetime, dt.datetime]]
          ) -> List[Tuple[dt.datetime, dt.datetime]]:
    """Merge intersecting intervals into a sorted list."""
    if not intervals:
        return []
    intervals.sort(key=lambda x: x[0])
    out = [intervals[0]]
    for s, e in intervals[1:]:
        ps, pe = out[-1]
        if s <= pe:
            out[-1] = (ps, max(pe, e))
        else:
            out.append((s, e))
    return out


BerthRecord = Tuple[dt.datetime, dt.datetime, float]       # (start, end, ship_len)

DAY_START = time(9, 0)
DAY_END   = time(18, 0)

# ───────────────────────────── planner ──────────────────────────────
class BaselinePlanner:
    """
    Greedy «first-free» planner respecting *capacity* (Σ length ≤ maxLength).

    Added rules 2025-06:
    • fuelType matches terminal's fuelSupported
    • requiresCustomsClearance → 2 h inspection at RAID terminal (09-18 UTC)
    • requiresPilot → if end after 18:00 → idle until 09:00 next day
    • arrival window is met, otherwise the vessel is let through
    • temperature/hazard/priority affect the order of processing
    """

    # ───────── init ─────────
    def __init__(self, req: PlanRequestModel):
        self.req = req

        self.timeline: Dict[int, List[BerthRecord]] = defaultdict(list)
        self.term_map: Dict[int, TerminalModel] = {t.id: t for t in req.port.terminals}

        # terminal raid (for inspection) - search by id==0 or name "raid"
        raid = next((t for t in req.port.terminals
                     if t.id == 0 or (t.name or "").lower() == "raid"), None)
        self.raid_tid = raid.id if raid else None

        # weather / closures
        self.weather = merge([(e.start, e.end) for e in req.conditions.weatherEvents])
        self.term_down: Dict[int, List[Tuple[dt.datetime, dt.datetime]]] = defaultdict(list)
        for ev in req.conditions.terminalClosures:
            self.term_down[ev.terminalId].append((ev.start, ev.end))
        self.term_down = {k: merge(v) for k, v in self.term_down.items()}

        log.info("BaselinePlanner: ships=%d, terminals=%d, raid=%s",
                 len(req.ships), len(req.port.terminals), self.raid_tid)

    # ───────── static checks ─────────
    def _fits_static(self, ship: ShipModel, term: TerminalModel) -> bool:
        return (
                ship.length <= term.maxLength and
                ship.draft  <= term.maxDraft  and
                ship.cargoType in term.allowedCargoTypes and
                (not term.fuelSupported or ship.fuelType in term.fuelSupported)
        )

    def _free(self, tid: int, s: dt.datetime, e: dt.datetime, ship_len: float) -> bool:
        # 1) Weather / Closings
        for xs, xe in self.weather:
            if overlap(xs, xe, s, e):
                return False
        for xs, xe in self.term_down.get(tid, []):
            if overlap(xs, xe, s, e):
                return False

        # 2) Capacity
        occupied = sum(l for xs, xe, l in self.timeline[tid]
                       if overlap(xs, xe, s, e))
        return occupied + ship_len <= self.term_map[tid].maxLength

    # ───────── earliest slot on terminal ─────────
    def _earliest_slot(self, ship: ShipModel, tid: int,
                       not_before: dt.datetime) -> dt.datetime:
        cur = not_before
        dur = timedelta(hours=max(ship.estDurationHours, 0.0001))

        while True:
            end = cur + dur
            if self._free(tid, cur, end, ship.length):
                return cur

            nxt: List[dt.datetime] = []
            nxt += [xe for xs, xe, _ in self.timeline[tid] if overlap(xs, xe, cur, end)]
            nxt += [xe for xs, xe in self.weather if overlap(xs, xe, cur, end)]
            nxt += [xe for xs, xe in self.term_down.get(tid, []) if overlap(xs, xe, cur, end)]
            cur = min(nxt) if nxt else end

    # ───────── customs raid ─────────
    def _schedule_customs(self, ship: ShipModel) -> Optional[dt.datetime]:
        """Returns ETA after inspection or None if the raid is unavailable."""
        if not self.raid_tid:
            log.warning("Ship %s needs customs, but raid terminal not found", ship.id)
            return None

        start = max(ship.arrivalTime, ship.arrivalWindowStart or ship.arrivalTime)

        customs_dur = timedelta(hours=2)
        cur = start

        while True:
            # adjusting to daytime
            if not DAY_START <= cur.time() < DAY_END:
                # transfer to 09:00 the next day
                cur = dt.datetime.combine(cur.date() + timedelta(days=1), DAY_START, tzinfo=cur.tzinfo)
            end = cur + customs_dur

            if self._free(self.raid_tid, cur, end, ship.length):
                insort(self.timeline[self.raid_tid], (cur, end, ship.length))
                log.info("🛃 %s customs RAID  %s → %s",
                         ship.id, cur.isoformat(timespec='seconds'),
                         end.isoformat(timespec='seconds'))
                return end  # new ETA

            # otherwise we jump to the end of the next conflict
            nxt = [xe for xs, xe, _ in self.timeline[self.raid_tid] if overlap(xs, xe, cur, end)]
            nxt += [xe for xs, xe in self.term_down.get(self.raid_tid, []) if overlap(xs, xe, cur, end)]
            nxt += [xe for xs, xe in self.weather if overlap(xs, xe, cur, end)]
            cur = min(nxt) if nxt else end

    # ───────── main build ─────────
    def build(self) -> List[dict]:
        # sort priority
        def order_key(s: ShipModel):
            hazard = 0 if (s.hazardClass or "").strip() else 1
            temp   = 0 if s.temperatureControlled else 1
            prio   = 0 if (s.priority or "normal") == "high" else 1
            return (hazard, temp, prio, s.arrivalTime)

        ships = sorted(self.req.ships, key=order_key)

        assigned, skipped = 0, 0
        schedule: List[dict] = []

        for ship in ships:
            eta = ship.arrivalTime

            # we guarantee that inside the window
            if ship.arrivalWindowStart and eta < ship.arrivalWindowStart:
                eta = ship.arrivalWindowStart
            if ship.arrivalWindowEnd and eta > ship.arrivalWindowEnd:
                log.warning("⚠  %s skipped: ETA after window end", ship.id)
                skipped += 1
                continue

            # customs inspection
            if ship.requiresCustomsClearance:
                eta = self._schedule_customs(ship)
                if eta is None:   # raid unavailable
                    skipped += 1
                    continue

            best_tid, best_start = None, None
            for term in self.req.port.terminals:
                if not self._fits_static(ship, term):
                    continue
                st = self._earliest_slot(ship, term.id, eta)
                # don't go out the window
                if ship.arrivalWindowEnd and st > ship.arrivalWindowEnd:
                    continue
                if best_start is None or st < best_start:
                    best_tid, best_start = term.id, st

            if best_tid is None:
                skipped += 1
                log.warning("⚠  Ship %s skipped — no berth fits", ship.id)
                continue

            end = best_start + timedelta(hours=ship.estDurationHours)

            # pilot waiting: until 09:00 UTC if required
            if ship.requiresPilot and end.time() > DAY_END:
                wait = dt.datetime.combine(end.date() + timedelta(days=1),
                                           DAY_START, tzinfo=end.tzinfo)
                log.info("🕓 %s waits for pilot until %s", ship.id,
                         wait.isoformat(timespec='seconds'))
                end = wait

            insort(self.timeline[best_tid], (best_start, end, ship.length))
            assigned += 1

            schedule.append(dict(
                vesselId   = ship.id,
                terminalId = best_tid,
                startTime  = best_start.replace(tzinfo=dt.timezone.utc),
                endTime    = end.replace(tzinfo=dt.timezone.utc)
            ))

            log.info("✅  %s → T%s  %s → %s",
                     ship.id, best_tid,
                     schedule[-1]["startTime"].isoformat(timespec='seconds').replace("+00:00", "Z"),
                     schedule[-1]["endTime"].isoformat(timespec='seconds').replace("+00:00", "Z"))

        log.info("=== baseline finished: assigned=%d  skipped=%d ===", assigned, skipped)

        return schedule