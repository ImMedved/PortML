from __future__ import annotations

import datetime as dt
import logging
import math
from bisect import insort
from collections import defaultdict
from datetime import timedelta, time
from typing import Dict, List, Tuple

from app.models.schemas import PlanRequestModel, ShipModel, TerminalModel

log = logging.getLogger(__name__)

def overlap(a0: dt.datetime, a1: dt.datetime,
            b0: dt.datetime, b1: dt.datetime) -> bool:
    return a0 < b1 and b0 < a1

def merge(xs: List[Tuple[dt.datetime, dt.datetime]]
          ) -> List[Tuple[dt.datetime, dt.datetime]]:
    if not xs:
        return []
    xs.sort(key=lambda p: p[0])
    out = [xs[0]]
    for s, e in xs[1:]:
        ps, pe = out[-1]
        if s <= pe:
            out[-1] = (ps, max(pe, e))
        else:
            out.append((s, e))
    return out

BerthRec = Tuple[dt.datetime, dt.datetime, float]
DAY_START, DAY_END = time(9), time(18)

class BaselinePlanner:

    def __init__(self, req: PlanRequestModel):
        self.req = req
        self.timeline: Dict[int, List[BerthRec]] = defaultdict(list)
        self.term_map: Dict[int, TerminalModel] = {t.id: t for t in req.port.terminals}

        raid = next((t for t in req.port.terminals
                     if t.id == 0 or (t.name or "").lower() == "raid"), None)
        self.raid_tid: int | None = raid.id if raid else None

        self.weather = merge([(e.start, e.end) for e in req.conditions.weatherEvents])
        self.term_down: Dict[int, List[Tuple[dt.datetime, dt.datetime]]] = defaultdict(list)
        for ev in req.conditions.terminalClosures:
            self.term_down[ev.terminalId].append((ev.start, ev.end))
        self.term_down = {k: merge(v) for k, v in self.term_down.items()}

        self._pilot_free_from: dt.datetime = dt.datetime.min      # single pilot

        log.info("BaselinePlanner: ships=%d, terminals=%d, raid=%s",
                 len(req.ships), len(req.port.terminals), self.raid_tid)

    # ───────── customs ─────────
    def _schedule_customs(self, ship: ShipModel) -> tuple[dt.datetime, dt.datetime, dt.datetime] | None:
        if self.raid_tid is None:
            log.warning("Ship %s needs customs, but Raid terminal not found", ship.id)
            return None

        cur = max(ship.arrivalTime, ship.arrivalWindowStart or ship.arrivalTime)
        dur = timedelta(hours=math.ceil(ship.length / 100))

        while True:
            if not DAY_START <= cur.time() < DAY_END:     # shift to next 09-00
                cur = dt.datetime.combine(cur.date() + timedelta(days=1), DAY_START)
            end = cur + dur
            if self._free(self.raid_tid, cur, end, ship.length):
                insort(self.timeline[self.raid_tid], (cur, end, ship.length))
                log.info("🛃 %s customs  %s → %s", ship.id,
                         cur.isoformat(timespec='seconds'),
                         end.isoformat(timespec='seconds'))
                return end, cur, end                      # ETA_after, start, end

            nxt: List[dt.datetime] = []
            nxt += [xe for xs, xe, _ in self.timeline[self.raid_tid] if overlap(xs, xe, cur, end)]
            nxt += [xe for xs, xe in self.term_down.get(self.raid_tid, []) if overlap(xs, xe, cur, end)]
            nxt += [xe for xs, xe in self.weather if overlap(xs, xe, cur, end)]
            cur = min(nxt) if nxt else end

    # ───────── util ─────────
    @staticmethod
    def _fits(ship: ShipModel, term: TerminalModel) -> bool:
        return (ship.length <= term.maxLength and
                ship.draft  <= term.maxDraft  and
                ship.cargoType in term.allowedCargoTypes and
                (not term.fuelSupported or ship.fuelType in term.fuelSupported))

    def _free(self, tid: int, s: dt.datetime, e: dt.datetime, ship_len: float) -> bool:
        for xs, xe in self.weather:
            if overlap(xs, xe, s, e):
                return False
        for xs, xe in self.term_down.get(tid, []):
            if overlap(xs, xe, s, e):
                return False
        occ = sum(l for xs, xe, l in self.timeline[tid] if overlap(xs, xe, s, e))
        return occ + ship_len <= self.term_map[tid].maxLength

    def _earliest(self, ship: ShipModel, tid: int, not_before: dt.datetime) -> dt.datetime:
        cur, dur = not_before, timedelta(hours=ship.estDurationHours)
        while True:
            end = cur + dur
            if self._free(tid, cur, end, ship.length):
                return cur
            nxt: List[dt.datetime] = []
            nxt += [xe for xs, xe, _ in self.timeline[tid] if overlap(xs, xe, cur, end)]
            nxt += [xe for xs, xe in self.weather if overlap(xs, xe, cur, end)]
            nxt += [xe for xs, xe in self.term_down.get(tid, []) if overlap(xs, xe, cur, end)]
            cur = min(nxt) if nxt else end

    # ───────── build plan ─────────
    def build(self) -> List[dict]:
        key = lambda s: (
            0 if (s.hazardClass or "").strip() else 1,
            0 if s.temperatureControlled else 1,
            0 if (s.priority or "normal") == "high" else 1,
            s.arrivalTime
        )
        ships = sorted(self.req.ships, key=key)

        out: List[dict] = []
        skipped = assigned = 0

        for ship in ships:
            eta, c_start, c_end = ship.arrivalTime, None, None

            # arrival window
            if ship.arrivalWindowStart and eta < ship.arrivalWindowStart:
                eta = ship.arrivalWindowStart
            if ship.arrivalWindowEnd and eta > ship.arrivalWindowEnd:
                log.warning("⚠ %s skipped: ETA after window end", ship.id)
                skipped += 1
                continue

            # customs
            if ship.requiresCustomsClearance:
                res = self._schedule_customs(ship)
                if res is None:
                    skipped += 1
                    continue
                eta, c_start, c_end = res

            # pilot gap
            if ship.requiresPilot and eta - timedelta(hours=1) < self._pilot_free_from:
                eta = self._pilot_free_from + timedelta(hours=1)

            # choose berth
            best_tid, best_st = None, None
            for t in self.req.port.terminals:
                if not self._fits(ship, t):
                    continue
                st = self._earliest(ship, t.id, eta)
                if ship.requiresPilot and st - timedelta(hours=1) < self._pilot_free_from:
                    st = self._earliest(ship, t.id,
                                        self._pilot_free_from + timedelta(hours=1))
                if ship.arrivalWindowEnd and st > ship.arrivalWindowEnd:
                    continue
                if best_st is None or st < best_st:
                    best_tid, best_st = t.id, st

            if best_tid is None:
                skipped += 1
                log.warning("⚠ Ship %s skipped — no berth fits", ship.id)
                continue

            end = best_st + timedelta(hours=ship.estDurationHours)
            if ship.requiresPilot and end.time() > DAY_END:
                end = dt.datetime.combine(end.date() + timedelta(days=1), DAY_START)
            insort(self.timeline[best_tid], (best_st, end, ship.length))

            if ship.requiresPilot:
                self._pilot_free_from = best_st

            # customs row kept as raid-id for schema compatibility
            if c_start and c_end:
                out.append(dict(
                    vesselId   = ship.id,
                    terminalId = self.raid_tid,         # <-- int!
                    startTime  = c_start.replace(tzinfo=dt.timezone.utc),
                    endTime    = c_end.replace(tzinfo=dt.timezone.utc)
                ))

            out.append(dict(
                vesselId   = ship.id,
                terminalId = best_tid,
                startTime  = best_st.replace(tzinfo=dt.timezone.utc),
                endTime    = end.replace(tzinfo=dt.timezone.utc)
            ))
            assigned += 1
            log.info("✅ %s → T%s  %s – %s",
                     ship.id, best_tid,
                     best_st.isoformat(timespec='seconds')+'Z',
                     end.isoformat(timespec='seconds')+'Z')

        log.info("=== baseline finished: assigned=%d skipped=%d ===", assigned, skipped)
        return out
