"""
boosting.py
TODO:
A very light-weight “boosting” planner:
    • сортирует суда по убыванию estDurationHours
    • пытается минимизировать суммарное ожидание
Идея: longer jobs first ≈ приоритет крупным судам.
Позже здесь можно обучить реальный GradientBoostingRegressor
и оценивать кандидаты, но сейчас алгоритм действительно иной,
чем baseline → UI увидит различие.
"""
from datetime import datetime, timedelta
from app.models.schemas import *
from app.services.baseline import BaselinePlanner


class BoostingPlanner(BaselinePlanner):
    """Reuses BaselinePlanner internals, but changes the order of the ships iterated over."""
    def build(self) -> list[dict]:
        # sort ships by processing time (desc), then ETA
        self.req.ships.sort(key=lambda s: (-s.estDurationHours, s.arrivalTime))
        return super().build()
