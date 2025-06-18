"""
Creates FastAPI instance, mounts v1 API router and wires
startup / shutdown hooks (e.g. model preload in future versions).
"""
from fastapi import FastAPI
from app.api.v1.endpoints import router as api_v1
from app.core.logging import configure_logging
import logging

configure_logging()
app = FastAPI(title="PortManager ML-service", version="1.0")
log = logging.getLogger(__name__)
log.info("✅ LOGGING WORKS IN MAIN")

app.include_router(api_v1, prefix="/v1")

@app.get("/health", tags=["health"])
def health() -> dict[str, str]:
    """Simple liveness probe used by backend & docker-compose."""
    return {"status": "ok"}
