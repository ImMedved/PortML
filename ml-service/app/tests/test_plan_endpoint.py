from fastapi.testclient import TestClient
from app.main import app
from app.models.schemas import PlanningAlgorithm

client = TestClient(app)

def test_plan_baseline():
    sample = {
        "port": {
            "terminals": [{
                "id": 1,
                "name": "A",
                "maxLength": 300,
                "maxDraft": 12,
                "allowedCargoTypes": ["container"]
            }],
            "startTime": "2025-06-01T00:00:00Z",
            "endTime":   "2025-06-02T00:00:00Z"
        },
        "ships": [{
            "id": "V1",
            "arrivalTime": "2025-06-01T05:00:00Z",
            "length": 200,
            "draft": 8,
            "cargoType": "container",
            "estDurationHours": 10
        }],
        "algorithm": "baseline"
    }
    resp = client.post("/v1/plan", json=sample)
    assert resp.status_code == 200
    body = resp.json()
    assert body["algorithmUsed"] == PlanningAlgorithm.baseline.value
    assert body["metrics"]["totalScheduledShips"] == 1
