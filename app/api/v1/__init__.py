from fastapi import APIRouter
from app.api.v1.endpoints.ingest import router as ingest_router

v1_router = APIRouter()
v1_router.include_router(ingest_router)
