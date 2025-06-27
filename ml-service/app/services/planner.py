import datetime as dt
import statistics

from app.models.schemas import (
    AssignmentModel,
    MetricsModel,
    PlanRequestModel,
    PlanResponseModel,
    PlanningAlgorithm,
)
from app.services import get_planner

# helpers
def _utilisation(schedule: list[dict], port) -> dict[int, float]:
    horizon_hours = (port.endTime - port.startTime).total_seconds() / 3600
    util = {t.id: 0 for t in port.terminals}
    for asg in schedule:
        dur = (asg["endTime"] - asg["startTime"]).total_seconds() / 3600
        util[asg["terminalId"]] += dur
    return {k: round(v / horizon_hours, 3) for k, v in util.items()}

# main facade
def plan(request: PlanRequestModel) -> PlanResponseModel:
    scheduler = get_planner(request)
    schedule = scheduler.build()          # ← here are more datetimes (aware)

    waits = []
    for asg in schedule:
        ship  = next(s for s in request.ships if s.id == asg["vesselId"])
        start = asg["startTime"]
        eta   = ship.arrivalTime
        if eta.tzinfo is None:             # if without zone - we assume that it is UTC
            eta = eta.replace(tzinfo=start.tzinfo or dt.timezone.utc)
        waits.append(max((start - eta).total_seconds() / 3600, 0))

    metrics = MetricsModel(
        totalWaitingTimeHours = round(sum(waits), 2),
        avgWaitingTimeHours   = round(statistics.mean(waits), 2) if waits else 0,
        maxWaitingTimeHours   = round(max(waits), 2) if waits else 0,
        utilizationByTerminal = _utilisation(schedule, request.port),
        totalScheduledShips   = len(schedule),
    )

    # Pydantic will automatically convert aware-datetime to ISO strings with +00:00
    return PlanResponseModel(
        schedule      = [AssignmentModel(**asg) for asg in schedule],
        metrics       = metrics,
        algorithmUsed = request.algorithm or PlanningAlgorithm.baseline,
        scenarioId    = None,
    )
