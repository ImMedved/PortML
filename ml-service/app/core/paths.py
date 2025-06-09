from pathlib import Path
BASE_DIR = Path(__file__).resolve().parents[2]   # …/ml-service
DATA_DIR = BASE_DIR / "data"
DATA_DIR.mkdir(exist_ok=True)
from app.core.config import get_settings  # back-compat