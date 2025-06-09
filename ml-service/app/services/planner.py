"""
Facade: selects an algorithm, builds a PlanResponseModel, calculates KPIs.
"""
import statistics
from datetime import timedelta

from app.models.schemas import (
    AssignmentModel,
    MetricsModel,
    PlanRequestModel,
    PlanResponseModel,
    PlanningAlgorithm,
)
from app.services import get_planner

def _utilisation(schedule: list[dict], port) -> dict[int, float]:
    """terminal-id → utilisation ratio over whole horizon."""
    horizon_hours = (port.endTime - port.startTime).total_seconds() / 3600
    util: dict[int, float] = {t.id: 0 for t in port.terminals}
    for asg in schedule:
        dur = (asg["endTime"] - asg["startTime"]).total_seconds() / 3600
        util[asg["terminalId"]] += dur
    return {k: round(v / horizon_hours, 3) for k, v in util.items()}

def plan(request: PlanRequestModel) -> PlanResponseModel:
    scheduler = get_planner(request)
    schedule = scheduler.build()

    # KPI
    waits = [
        max(
            (
                    asg["startTime"]
                    - next(ship for ship in request.ships if ship.id == asg["shipId"]).arrivalTime
            ).total_seconds()
            / 3600,
            0,
            )
        for asg in schedule
    ]
    metrics = MetricsModel(
        totalWaitingTimeHours=round(sum(waits), 2),
        avgWaitingTimeHours=round(statistics.mean(waits), 2) if waits else 0,
        maxWaitingTimeHours=round(max(waits), 2) if waits else 0,
        utilizationByTerminal=_utilisation(schedule, request.port),
        totalScheduledShips=len(schedule),
    )

    return PlanResponseModel(
        schedule=[AssignmentModel(**asg) for asg in schedule],
        metrics=metrics,
        algorithmUsed=scheduler.req.algorithm or PlanningAlgorithm.baseline,
        scenarioId=None,
    )


def dummy_plan(algo: PlanningAlgorithm) -> PlanResponseModel:
    """
    One-line version used by /pairwisePlans?dummy=true
    """
    return PlanResponseModel(
        schedule=[],
        metrics=MetricsModel(
            totalWaitingTimeHours=0,
            avgWaitingTimeHours=0,
            maxWaitingTimeHours=0,
            utilizationByTerminal={},
            totalScheduledShips=0,
        ),
        algorithmUsed=algo,
        scenarioId=0,
    )
