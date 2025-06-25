"""
Stub-replacement for deep RL: gives a *random-greedy* (epsilon-greedy 0.3)
variant. This is a **working** algorithm that periodically
shuffles the order of the ships - the user in a pairwise test
will sometimes choose baseline / boosting, sometimes RL.
"""
import random
from app.models.schemas import *
from app.services.baseline import BaselinePlanner


class RandomRLPlanner(BaselinePlanner):
    EPS = 0.3
    def build(self) -> list[dict]:
        if random.random() < self.EPS:
            random.shuffle(self.req.ships)
        return super().build()

