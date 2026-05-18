from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.core.config import settings
from app.api.v1 import v1_router

app = FastAPI(
    title=settings.PROJECT_NAME,
    version="1.0.0",
    description="Headless Multi-Agent Urban Simulation Core Engine"
)

# Mount loose CORS origins to allow clean integration with our React mapping dashboard
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(v1_router, prefix="/api/v1")

@app.get("/health", tags=["System Diagnostics"])
async def health_check():
    return {"status": "healthy", "project_id": settings.GCP_PROJECT_ID}
