from datetime import datetime, timedelta
from app.models.schemas import *

class BaselinePlanner:
    """Greedy “first-come first-served” berth allocation."""

    def __init__(self, request: PlanRequestModel):
        self.req = request
        self.timeline: dict[int, list[tuple[datetime, datetime]]] = {
            t.id: [] for t in request.port.terminals
        }

    # ---- helpers --------------------------------------------------
    def _earliest_slot(self, term_id: int, arr: datetime, dur_hours: float) -> datetime:
        occupied = sorted(self.timeline[term_id], key=lambda seg: seg[0])
        cur = arr
        delta = timedelta(hours=dur_hours)
        for start, end in occupied:
            if cur + delta <= start:
                return cur
            cur = max(cur, end)
        return cur

    def _fits_terminal(self, ship: ShipModel, term: TerminalModel) -> bool:
        return (
                ship.length     <= term.maxLength and
                ship.draft      <= term.maxDraft  and
                ship.cargoType in term.allowedCargoTypes
        )

    # ---- main -----------------------------------------------------
    def build(self) -> list[dict]:
        schedule: list[dict] = []
        for ship in sorted(self.req.ships, key=lambda s: s.arrivalTime):
            best_term, best_start = None, None
            for term in self.req.port.terminals:
                if not self._fits_terminal(ship, term):
                    continue
                ts = self._earliest_slot(term.id, ship.arrivalTime, ship.estDurationHours)
                if best_start is None or ts < best_start:
                    best_term, best_start = term, ts
            if best_term is None:
                continue
            end = best_start + timedelta(hours=ship.estDurationHours)
            self.timeline[best_term.id].append((best_start, end))
            schedule.append({
                "vesselId":   ship.id,
                "terminalId": best_term.id,
                "startTime":  best_start,
                "endTime":    end
            })
        return schedule
