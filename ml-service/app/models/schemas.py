from __future__ import annotations
from datetime import datetime
from enum import Enum
from typing import Dict, List, Literal, Optional

from pydantic import BaseModel, Field

# ── enums ──────────────────────────────────────────────────────────
class PlanningAlgorithm(str, Enum):
    baseline = "baseline"
    boosting = "boosting"
    RL       = "RL"
    pairwise = "pairwise"

# ── port description ───────────────────────────────────────────────
class TerminalModel(BaseModel):
    id: int
    name: Optional[str]
    maxLength: float
    maxDraft: float
    allowedCargoTypes: List[str]

class PortModel(BaseModel):
    terminals: List[TerminalModel]
    startTime: datetime
    endTime: datetime

# ── scenario ───────────────────────────────────────────────────────
class ShipModel(BaseModel):
    id: str
    arrivalTime: datetime
    length: float
    draft: float
    cargoType: str
    estDurationHours: float

class TerminalClosureModel(BaseModel):
    terminalId: int
    start: datetime
    end: datetime
    reason: Optional[str]

class WeatherEventModel(BaseModel):
    start: datetime
    end: datetime
    description: Optional[str]

class ConditionsModel(BaseModel):
    terminalClosures: List[TerminalClosureModel] = Field(default_factory=list)
    weatherEvents: List[WeatherEventModel]       = Field(default_factory=list)

# ── request / response ─────────────────────────────────────────────
class PlanRequestModel(BaseModel):
    port: PortModel
    ships: List[ShipModel]
    conditions: ConditionsModel = ConditionsModel()
    algorithm: Optional[PlanningAlgorithm] = PlanningAlgorithm.baseline

class AssignmentModel(BaseModel):
    vesselId:  str  = Field(alias="vesselId")
    terminalId: int
    startTime:  datetime
    endTime:    datetime

class MetricsModel(BaseModel):
    totalWaitingTimeHours: float
    avgWaitingTimeHours:   float
    maxWaitingTimeHours:   float
    utilizationByTerminal: Dict[int, float]
    totalScheduledShips:   int

class PlanResponseModel(BaseModel):
    schedule: List[AssignmentModel]
    metrics:  MetricsModel
    algorithmUsed: PlanningAlgorithm
    scenarioId: Optional[int] = None

# ── pairwise ───────────────────────────────────────────────────────
class PairwiseRequestModel(BaseModel):
    comparisonId: str
    planA: PlanResponseModel
    planB: PlanResponseModel
    question: Optional[str] = None

class PairwiseFeedbackModel(BaseModel):
    comparisonId: str
    chosenPlan: Literal["A", "B"]
