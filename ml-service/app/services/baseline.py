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
    """True, если интервалы [a0,a1) и [b0,b1) пересекаются."""
    return a0 < b1 and b0 < a1


def merge(intervals: List[Tuple[dt.datetime, dt.datetime]]
          ) -> List[Tuple[dt.datetime, dt.datetime]]:
    """Сливаем пересекающиеся интервалы в сортированный список."""
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


# ─────────────────────────────── planner ────────────────────────────
class BaselinePlanner:
    """
    Жадный «первый свободный слот».

    * учитывает ETA, длительность, погоду и закрытия терминалов;
    * на одном терминале одновременно ≤ 1 судна;
    * наружу отправляет **aware-datetime** (UTC) —
      UI спокойно парсит `+00:00` / `Z`, а backend остаётся с типами `String`.
    """

    def __init__(self, req: PlanRequestModel):
        self.req = req
        self.timeline: Dict[int, List[Tuple[dt.datetime, dt.datetime]]] = defaultdict(list)

        # чёрные интервалы
        self.weather = merge([(e.start, e.end) for e in req.conditions.weatherEvents])
        self.term_down: Dict[int, List[Tuple[dt.datetime, dt.datetime]]] = defaultdict(list)
        for ev in req.conditions.terminalClosures:
            self.term_down[ev.terminalId].append((ev.start, ev.end))
        self.term_down = {k: merge(v) for k, v in self.term_down.items()}

        log.info(
            "BaselinePlanner: ships=%d, terminals=%d, weather=%d, closures=%d",
            len(req.ships), len(req.port.terminals),
            len(self.weather), sum(len(v) for v in self.term_down.values()),
        )

    # ---------- checks ----------------------------------------------
    @staticmethod
    def _fits(ship: ShipModel, term: TerminalModel) -> bool:
        return ship.cargoType in term.allowedCargoTypes

    def _free(self, tid: int, s: dt.datetime, e: dt.datetime) -> bool:
        for xs, xe in self.timeline[tid]:
            if overlap(xs, xe, s, e):
                return False
        for xs, xe in self.weather:
            if overlap(xs, xe, s, e):
                return False
        for xs, xe in self.term_down.get(tid, []):
            if overlap(xs, xe, s, e):
                return False
        return True

    def _earliest_slot(self, ship: ShipModel, tid: int) -> dt.datetime:
        cur = ship.arrivalTime
        dur = timedelta(hours=max(ship.estDurationHours, 0.0001))
        while True:
            end = cur + dur
            if self._free(tid, cur, end):
                return cur
            nxt: List[dt.datetime] = []
            nxt += [xe for xs, xe in self.timeline[tid] if overlap(xs, xe, cur, end)]
            nxt += [xe for xs, xe in self.weather       if overlap(xs, xe, cur, end)]
            nxt += [xe for xs, xe in self.term_down.get(tid, []) if overlap(xs, xe, cur, end)]
            cur = min(nxt) if nxt else end

    # ---------- main -------------------------------------------------
    def build(self) -> List[dict]:
        ships = sorted(self.req.ships, key=lambda s: s.arrivalTime)
        schedule: List[dict] = []
        assigned = skipped = 0

        log.info("=== ⚓ BaselinePlanner.build() ===")

        for ship in ships:
            # ищем терминал и старт
            best_tid, best_start = None, None
            for term in self.req.port.terminals:
                if not self._fits(ship, term):
                    continue
                st = self._earliest_slot(ship, term.id)
                if best_start is None or st < best_start:
                    best_tid, best_start = term.id, st

            if best_tid is None:
                skipped += 1
                log.warning("⚠  Ship %s skipped — no suitable terminal", ship.id)
                continue

            end = best_start + timedelta(hours=ship.estDurationHours)
            insort(self.timeline[best_tid], (best_start, end))
            assigned += 1

            # делаем aware-datetime (UTC)
            start_dt = best_start.replace(tzinfo=dt.timezone.utc)
            end_dt   = end.replace(tzinfo=dt.timezone.utc)

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
