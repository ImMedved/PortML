import json, logging, time
from app.core.paths import DATA_DIR, get_settings

log = logging.getLogger(__name__)

def retrain_model() -> None:
    """Reads every feedback line and pretends to train a model."""
    fb_file = DATA_DIR / "feedback.jsonl"
    if not fb_file.exists():
        log.info("no feedback yet, nothing to train.")
        return
    with fb_file.open("r", encoding="utf-8") as fh:
        lines = [json.loads(l) for l in fh]
    log.info("training on %d preference pairs …", len(lines))
    time.sleep(0.3) # fake workload
    # persist “model”
    model_path = DATA_DIR / "model.pkl"
    model_path.touch()
    log.info("model saved to %s", model_path)
