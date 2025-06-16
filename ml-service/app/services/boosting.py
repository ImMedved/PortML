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
    def build(self) -> list[dict]:
        self.req.ships.sort(key=lambda s: (-s.estDurationHours, s.arrivalTime))
        return super().build()

