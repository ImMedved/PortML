"""
REST wrappers around scheduler service functions + feedback reception.
"""
import datetime, json, logging, uuid
from fastapi import APIRouter, HTTPException
from app.models.schemas import *
from app.services import planner, get_planner
from app.training.trainer import retrain_model
from app.core.paths import DATA_DIR

router = APIRouter()
log = logging.getLogger(__name__)

# plan
@router.post("/plan", response_model=PlanResponseModel)
def generate_plan(req: PlanRequestModel) -> PlanResponseModel:
    """Build a single plan using the selected algorithm."""
    return planner.plan(req) # direct build, KPI via wrapper

# feedback
@router.post("/feedback")
def feedback(data: PairwiseFeedbackModel) -> dict[str, str]:
    """Save the feedback string to data/feedback.jsonl."""
    record = data.model_dump()
    record["ts"] = datetime.datetime.utcnow().isoformat()
    fn = DATA_DIR / "feedback.jsonl"
    with fn.open("a", encoding="utf-8") as fh:
        fh.write(json.dumps(record) + "\n")
    log.info("feedback stored: %s", record["comparisonId"])
    return {"status": "ok"}

# train
@router.post("/train")
def train() -> dict[str, str]:
    """Launching offline retraining (fake)."""
    retrain_model()
    return {"status": "training started"}

# pairwise
@router.get("/pairwisePlans", response_model=PairwiseRequestModel)
def pairwise_plans(
        algoA: PlanningAlgorithm = PlanningAlgorithm.baseline,
        algoB: PlanningAlgorithm = PlanningAlgorithm.RL,
        dummy: bool = False,
) -> PairwiseRequestModel:
    """
    Issue two alternative plans for the same scenario.
    By default, the backend should pull /plan twice,
    but for the demo we leave the endpoint short.
    """
    if dummy:
        # генерация «пустой» пары для unit-тестов
        return PairwiseRequestModel(
            comparisonId="cmp-0",
            planA=planner.dummy_plan(algoA),
            planB=planner.dummy_plan(algoB),
            question="dummy",
        )
    raise HTTPException(
        status_code=400,
        detail="Backend requests pairwise: make two calls /v1/plan",
    )
