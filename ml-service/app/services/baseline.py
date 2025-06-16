from __future__ import annotations

from bisect import insort
from collections import defaultdict
from datetime import datetime, timedelta

from app.models.schemas import (
    PlanRequestModel,
    ShipModel,
    TerminalModel,
)

# ───────────────────────────────────────────────────────────────────
# helpers
# ───────────────────────────────────────────────────────────────────
def overlap(a0: datetime, a1: datetime, b0: datetime, b1: datetime) -> bool:
    """True when [a0,a1) ∩ [b0,b1) ≠ ∅."""
    return max(a0, b0) < min(a1, b1)


def merge_intervals(intervals: list[tuple[datetime, datetime]]) -> list[tuple[datetime, datetime]]:
    """Склеиваем пересекающиеся интервалы (для чёрных отверстий)."""
    if not intervals:
        return []
    intervals.sort(key=lambda x: x[0])
    merged = [intervals[0]]
    for s, e in intervals[1:]:
        ps, pe = merged[-1]
        if s <= pe:          # пересекается
            merged[-1] = (ps, max(pe, e))
        else:
            merged.append((s, e))
    return merged


# ───────────────────────────────────────────────────────────────────
# основной планировщик
# ───────────────────────────────────────────────────────────────────
class BaselinePlanner:
    """
    Жадное, но *корректное* расписание.

    • учитывает draft/тип груза (как раньше);
    • допускает одновременное пребывание нескольких судов,
      пока Σ(length) ≤ maxLength;
    • полностью исключает WeatherEvent и TerminalClosure;
    • выдаёт самый ранний возможный слот — ждать «лишнего» не будет.
    """

    # -----------------------------------------------------------------
    # init
    # -----------------------------------------------------------------
    def __init__(self, request: PlanRequestModel) -> None:
        self.req = request

        # по терминалам: [(start, end, length)]
        self.timeline: dict[int, list[tuple[datetime, datetime, float]]] = defaultdict(list)

        # чёрные интервалы: global + по терминалам
        self.global_down = merge_intervals(
            [(ev.start, ev.end) for ev in request.conditions.weatherEvents]
        )

        self.term_down: dict[int, list[tuple[datetime, datetime]]] = defaultdict(list)
        for cl in request.conditions.terminalClosures:
            self.term_down[cl.terminalId].append((cl.start, cl.end))
        for tid, arr in self.term_down.items():
            self.term_down[tid] = merge_intervals(arr)

        # чтобы быстро узнавать maxLength / static-check
        self.term_map: dict[int, TerminalModel] = {t.id: t for t in request.port.terminals}

    # -----------------------------------------------------------------
    # статические ограничения
    # -----------------------------------------------------------------
    def _static_ok(self, ship: ShipModel, term: TerminalModel) -> bool:
        return (
                ship.draft <= term.maxDraft
                and ship.cargoType in term.allowedCargoTypes
        )

    # -----------------------------------------------------------------
    # динамика: вместимость / отключения
    # -----------------------------------------------------------------
    def _blocked(self, term_id: int, s: datetime, e: datetime) -> bool:
        """попадает ли интервал под любое закрытие"""
        for xs, xe in self.global_down:
            if overlap(xs, xe, s, e):
                return True
        for xs, xe in self.term_down.get(term_id, []):
            if overlap(xs, xe, s, e):
                return True
        return False

    def _cap_ok(self, term_id: int, s: datetime, e: datetime, add_len: float) -> bool:
        """Σ(length) на всём отрезке не превышает limit"""
        limit = self.term_map[term_id].maxLength
        used = 0.0
        for as_, ae, ln in self.timeline[term_id]:
            if overlap(as_, ae, s, e):
                used += ln
                # оптимизация: early-exit
                if used + add_len > limit:
                    return False
        return used + add_len <= limit

    # -----------------------------------------------------------------
    # поиск earliest-слота
    # -----------------------------------------------------------------
    def _earliest_slot(self, ship: ShipModel, term_id: int) -> datetime:
        """Самый ранний момент ≥ ETA, свободный по ВСЕМ ограничениям."""
        cursor = ship.arrivalTime
        dur = timedelta(hours=ship.estDurationHours)
        limit = self.term_map[term_id].maxLength

        # подготовим список «точек изменения» загрузки:
        #  – конец любого судна
        #  – конец любого blackout-интервала
        change_points: set[datetime] = {cursor}
        change_points.update(e for _, e, _ in self.timeline[term_id])
        change_points.update(e for _, e in self.term_down.get(term_id, []))
        change_points.update(e for _, e in self.global_down)
        # работать будем в отсортированном виде
        change_points = sorted(change_points)

        while True:
            end = cursor + dur

            # 1. blackout?
            if self._blocked(term_id, cursor, end):
                # прыгаем к самому ближнему концу blackout
                jumps = [
                    xe
                    for xs, xe in (self.global_down + self.term_down.get(term_id, []))
                    if overlap(xs, xe, cursor, end)
                ]
                cursor = max(jumps)  # обязательно >= текущего end overlap-a
                continue

            # 2. вместимость?
            if self._cap_ok(term_id, cursor, end, ship.length):
                return cursor  # нашли!

            # 3. куда прыгать, если по длине не лезет?
            #    найдём первый конфликтующий отрезок и дождёмся его конца
            next_times = [
                ae
                for as_, ae, _ in self.timeline[term_id]
                if overlap(as_, ae, cursor, end)
            ]
            cursor = min(next_times) if next_times else end

            # safety-костыль: если «упёрлись» в последнее событие, расширяем горизонт
            if cursor not in change_points:
                insort(change_points, cursor)

    # -----------------------------------------------------------------
    # основной цикл
    # -----------------------------------------------------------------
    def build(self) -> list[dict]:
        schedule: list[dict] = []

        # порядок обхода судов может менять наследник, поэтому работаем
        # напрямую со списком внутри req
        ships = self.req.ships

        for ship in ships:
            best_tid, best_start = None, None

            for term in self.req.port.terminals:
                if not self._static_ok(ship, term):
                    continue

                start = self._earliest_slot(ship, term.id)
                if best_start is None or start < best_start:
                    best_tid, best_start = term.id, start

            if best_tid is None:
                # ship cannot be allocated (нет подходящих причалов)
                continue

            end = best_start + timedelta(hours=ship.estDurationHours)
            insort(self.timeline[best_tid], (best_start, end, ship.length))

            schedule.append(
                {
                    "vesselId": ship.id,
                    "terminalId": best_tid,
                    "startTime": best_start,
                    "endTime": end,
                }
            )

        return schedule
