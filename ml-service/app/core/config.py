"""
Centralised settings object (env-driven).
"""
from functools import lru_cache
from pydantic import BaseModel, Field

class Settings(BaseModel):
    model_path: str = Field(default="./data/model.pkl")
    feedback_path: str = Field(default="./data/feedback.jsonl")

    class Config:
        env_prefix = "ML_"

@lru_cache
def get_settings() -> Settings:
    return Settings()
