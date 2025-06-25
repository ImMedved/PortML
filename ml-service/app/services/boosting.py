"""
boosting.py
TODO:
A very light-weight “boosting” planner:
• sorts ships by descending estDurationHours
• tries to minimize total wait
Idea: longer jobs first ≈ priority to larger ships.
Later, a real GradientBoostingRegressor can be trained here
and evaluate candidates, but for now the algorithm is really different
than baseline → UI will see the difference.
"""
from datetime import datetime, timedelta
from app.models.schemas import *
from app.services.baseline import BaselinePlanner


class BoostingPlanner(BaselinePlanner):
    def build(self) -> list[dict]:
        self.req.ships.sort(key=lambda s: (-s.estDurationHours, s.arrivalTime))
        return super().build()

