from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    PROJECT_NAME: str = "CitySmart Social Digital Twin Backend"
    GCP_PROJECT_ID: str = "sinuous-branch-411610"
    GCP_REGION: str = "asia-northeast3"
    DATABASE_URL: Optional[str] = None
    GEMINI_API_KEY: Optional[str] = None
    
    class Config:
        env_file = ".env"
        extra = "ignore"

settings = Settings()
