from __future__ import annotations
from datetime import datetime
from enum import Enum
from typing import Dict, List, Optional, Literal

from pydantic import BaseModel, Field


# ─────────────── enums ───────────────
class PlanningAlgorithm(str, Enum):
    baseline = "baseline"
    boosting = "boosting"
    RL       = "RL"
    pairwise = "pairwise"


# ─────────────── port/terminal ───────────────
class TerminalModel(BaseModel):
    id: int
    name: Optional[str]            = None
    maxLength: float
    maxDraft:  float
    allowedCargoTypes: List[str]
    fuelSupported: List[str]       = Field(default_factory=list)   # NEW


class PortModel(BaseModel):
    terminals: List[TerminalModel]
    startTime: datetime
    endTime:   datetime


# ─────────────── ships / events ───────────────
class ShipModel(BaseModel):
    # identification / size
    id: str
    length: float
    draft:  float
    deadweight: float = 0.0                        # NEW

    # registry / type
    cargoType: str
    shipType:  Optional[str] = None                # NEW
    flagCountry: Optional[str] = None              # NEW
    imoNumber:  Optional[str] = None               # NEW

    # fuel / ecology
    fuelType: str                                  # NEW
    emissionRating: Optional[str] = None           # NEW

    # routing / timing
    arrivalTime: datetime
    arrivalWindowStart: Optional[datetime] = None  # NEW
    arrivalWindowEnd:   Optional[datetime] = None  # NEW
    expectedDelayHours: float = 0.0                # NEW

    estDurationHours: float

    arrivalPort: Optional[str] = None              # NEW
    nextPort:    Optional[str] = None              # NEW

    # special flags
    requiresCustomsClearance: bool = False         # NEW
    requiresPilot: bool = False                    # NEW
    temperatureControlled: bool = False            # NEW
    hazardClass: Optional[str] = None              # NEW
    priority: Optional[str] = None                 # keep old


# events
class TerminalClosureModel(BaseModel):
    terminalId: int
    start: datetime
    end:   datetime
    reason: Optional[str] = None


class WeatherEventModel(BaseModel):
    start: datetime
    end:   datetime
    description: Optional[str] = None


class ConditionsModel(BaseModel):
    terminalClosures: List[TerminalClosureModel] = Field(default_factory=list)
    weatherEvents:    List[WeatherEventModel]    = Field(default_factory=list)


# ─────────────── request / response ───────────────
class PlanRequestModel(BaseModel):
    port: PortModel
    ships: List[ShipModel]
    conditions: ConditionsModel = ConditionsModel()
    algorithm: Optional[PlanningAlgorithm] = PlanningAlgorithm.baseline


class AssignmentModel(BaseModel):
    vesselId:  str   = Field(alias="vesselId")
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


# ─────────────── pair-wise feedback ───────────────
class PairwiseRequestModel(BaseModel):
    comparisonId: str
    planA: PlanResponseModel
    planB: PlanResponseModel
    question: Optional[str] = None


class PairwiseFeedbackModel(BaseModel):
    comparisonId: str
    chosenPlan: Literal["A", "B"]
