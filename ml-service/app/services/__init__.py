"""
Factory for planner objects by algorithm code.
"""
from app.models.schemas import PlanningAlgorithm, PlanRequestModel
from app.services.baseline import BaselinePlanner
from app.services.boosting import BoostingPlanner
from app.services.rl import RandomRLPlanner


def get_planner(req: PlanRequestModel):
    algo = req.algorithm or PlanningAlgorithm.baseline
    if algo == PlanningAlgorithm.baseline:
        return BaselinePlanner(req)
    if algo == PlanningAlgorithm.boosting:
        return BoostingPlanner(req)
    if algo == PlanningAlgorithm.RL:
        return RandomRLPlanner(req)
    raise ValueError(f"Unsupported algorithm: {algo}")
