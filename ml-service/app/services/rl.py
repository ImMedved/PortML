"""
Stub-replacement for deep RL: даёт *случайно-жадный* (epsilon-greedy 0.3)
вариант.  Это **работающий** алгоритм, который периодически
перемешивает порядок судов – пользователь при pairwise-тесте
будет иногда выбирать baseline / boosting, иногда RL.
"""
import random
from app.models.schemas import *
from app.services.baseline import BaselinePlanner


class RandomRLPlanner(BaselinePlanner):

    EPS = 0.3  # 30 % chance to shuffle whole queue

    def build(self) -> list[dict]:
        if random.random() < self.EPS:
            random.shuffle(self.req.ships)
        # FIFO-greedy
        return super().build()
