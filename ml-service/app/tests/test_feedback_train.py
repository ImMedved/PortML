from fastapi.testclient import TestClient
from app.main import app
from pathlib import Path
from app.core.paths import DATA_DIR

client = TestClient(app)

def test_feedback_and_train(tmp_path: Path, monkeypatch):
    # redirect data dir to temp
    monkeypatch.setattr("app.core.paths.DATA_DIR", tmp_path)

    # store feedback
    fb = {"comparisonId": "cmp-1", "chosenPlan": "A"}
    r = client.post("/v1/feedback", json=fb)
    assert r.status_code == 200
    fb_file = tmp_path / "feedback.jsonl"
    assert fb_file.exists()

    # call train
    r = client.post("/v1/train")
    assert r.status_code == 200
    assert (tmp_path / "model.pkl").exists()
