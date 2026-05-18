from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PROJECT_NAME: str = "CitySmart Social Digital Twin Backend"
    GCP_PROJECT_ID: str = "sinuous-branch-411610"
    GCP_REGION: str = "asia-northeast3"
    
    class Config:
        env_file = ".env"
        extra = "ignore"

settings = Settings()
