from fastapi import APIRouter
from app.api.v1.endpoints.ingest import router as ingest_router
from app.api.v1.endpoints.simulation import router as sim_router
from app.api.v1.endpoints.views import router as views_router

v1_router = APIRouter()
v1_router.include_router(ingest_router)
v1_router.include_router(sim_router)
v1_router.include_router(views_router)
