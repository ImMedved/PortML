from __future__ import annotations

import datetime as dt
import logging
from bisect import insort
from collections import defaultdict
from datetime import timedelta
from typing import Dict, List, Tuple

from app.models.schemas import PlanRequestModel, ShipModel, TerminalModel

log = logging.getLogger(__name__)

# ─────────────────────────────── helpers ────────────────────────────
def overlap(a0: dt.datetime, a1: dt.datetime,
            b0: dt.datetime, b1: dt.datetime) -> bool:
    # True if intervals [a0,a1) and [b0,b1) intersect.
    return a0 < b1 and b0 < a1


def merge(intervals: List[Tuple[dt.datetime, dt.datetime]]
          ) -> List[Tuple[dt.datetime, dt.datetime]]:
    # Merge intersecting intervals into a sorted list.
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

# new schedule entry type
# each entry now stores the length of the vessel to calculate "density"

BerthRecord = Tuple[dt.datetime, dt.datetime, float]       # (start, end, ship_len)

# ─────────────────────────────── planner ────────────────────────────
class BaselinePlanner:
    """
    Greedy "first free slot" taking into account the *capacity* of the berth.

    * ETA, duration, weather, closures
    * N ships can be at the terminal at the same time, if
    sum(ship.length) ≤ terminal.maxLength
    * we give out aware-datetime (UTC) — UI/Backend parses without problems
    """

    def __init__(self, req: PlanRequestModel):
        self.req = req

        # schedule: terminal id → list (start, end, ship_len)
        self.timeline: Dict[int, List[BerthRecord]] = defaultdict(list)

        # terminal map (quick access to maxLength / allowedCargoTypes)
        self.term_map: Dict[int, TerminalModel] = {t.id: t for t in req.port.terminals}

        # black intervals
        self.weather = merge([(e.start, e.end) for e in req.conditions.weatherEvents])
        self.term_down: Dict[int, List[Tuple[dt.datetime, dt.datetime]]] = defaultdict(list)
        for ev in req.conditions.terminalClosures:
            self.term_down[ev.terminalId].append((ev.start, ev.end))
        self.term_down = {k: merge(v) for k, v in self.term_down.items()}

        log.info("BaselinePlanner: ships=%d, terminals=%d, weather=%d, closures=%d",
                 len(req.ships), len(req.port.terminals),
                 len(self.weather), sum(len(v) for v in self.term_down.values()))

    # checks
    def _fits_static(self, ship: ShipModel, term: TerminalModel) -> bool:
        # Quick static check (length/draft/type).
        return (
                ship.length <= term.maxLength and
                ship.draft  <= term.maxDraft  and
                ship.cargoType in term.allowedCargoTypes
        )

    def _free(self, tid: int, s: dt.datetime, e: dt.datetime, ship_len: float) -> bool:
        """
        Checking: is it possible to place a vessel of length `ship_len` to the terminal tid in the interval [s,e).
        """
        # 1) Weather / Closings
        for xs, xe in self.weather:
            if overlap(xs, xe, s, e):
                return False
        for xs, xe in self.term_down.get(tid, []):
            if overlap(xs, xe, s, e):
                return False

        # 2) Capacity
        occupied = sum(rec_len for xs, xe, rec_len in self.timeline[tid]
                       if overlap(xs, xe, s, e))
        capacity = self.term_map[tid].maxLength
        return occupied + ship_len <= capacity

    def _earliest_slot(self, ship: ShipModel, tid: int) -> dt.datetime:
        """
        Earliest moment ≥ ETA when space is available according to all rules.
        Takes into account the total length of already moored vessels.
        """
        cur = ship.arrivalTime
        dur = timedelta(hours=max(ship.estDurationHours, 0.0001))
        while True:
            end = cur + dur
            if self._free(tid, cur, end, ship.length):
                return cur

            # collect the nearest ends of all intersections
            nxt: List[dt.datetime] = []

            # other vessels
            nxt += [xe for xs, xe, _ in self.timeline[tid] if overlap(xs, xe, cur, end)]
            # weather
            nxt += [xe for xs, xe in self.weather if overlap(xs, xe, cur, end)]
            # term-closure
            nxt += [xe for xs, xe in self.term_down.get(tid, []) if overlap(xs, xe, cur, end)]

            cur = min(nxt) if nxt else end   # jump forward

    # main
    def build(self) -> List[dict]:
        ships = sorted(self.req.ships, key=lambda s: s.arrivalTime)
        schedule: List[dict] = []
        assigned = skipped = 0

        log.info("=== ⚓ BaselinePlanner.build() ===")

        for ship in ships:
            best_tid, best_start = None, None

            # looking for terminal/time
            for term in self.req.port.terminals:
                if not self._fits_static(ship, term):
                    continue
                st = self._earliest_slot(ship, term.id)
                if best_start is None or st < best_start:
                    best_tid, best_start = term.id, st

            if best_tid is None:
                skipped += 1
                log.warning("⚠  Ship %s skipped — no suitable terminal", ship.id)
                continue

            end = best_start + timedelta(hours=ship.estDurationHours)
            # add a record to control the capacity
            insort(self.timeline[best_tid], (best_start, end, ship.length))
            assigned += 1

            # aware-datetime (UTC) for answer
            start_dt = best_start.replace(tzinfo=dt.timezone.utc)
            end_dt   = end        .replace(tzinfo=dt.timezone.utc)

            log.info("✅  %s → T%s   %s → %s",
                     ship.id, best_tid,
                     start_dt.isoformat(timespec='seconds').replace('+00:00', 'Z'),
                     end_dt  .isoformat(timespec='seconds').replace('+00:00', 'Z'))

            schedule.append(
                dict(
                    vesselId   = ship.id,
                    terminalId = best_tid,
                    startTime  = start_dt,
                    endTime    = end_dt,
                )
            )

        log.info("=== ✅ complete: assigned=%d skipped=%d ===", assigned, skipped)
        return schedule
