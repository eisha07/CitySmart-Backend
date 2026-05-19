import os
import sqlalchemy.exc
from contextlib import asynccontextmanager
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from google.api_core.exceptions import GoogleAPICallError
from app.api.v1 import v1_router
from app.services.pubsub_worker import pubsub_worker
from app.core.logger import log_execution_time_middleware


# Define the server lifespan context controller to manage asynchronous hardware hooks cleanly
@asynccontextmanager
async def app_lifespan_handler(app: FastAPI):
    # --- Startup Event Sequence ---
    print("✨ Core Server Initialization Sequence Booting up...")

    # Securely set GCP application environment key references
    cred_file = "sinuous-branch-411610-d4e78e429c6c.json"
    if not os.path.exists(cred_file):
        cred_file = "temp_sa_key.json"
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = cred_file

    # Fire up our non-blocking Pub/Sub worker queue streaming listener
    pubsub_stream = pubsub_worker.start_listening()

    yield

    # --- Shutdown Event Sequence ---
    print(
        "🛑 Server Shutdown Triggered. Closing background network streaming channels..."
    )
    pubsub_stream.cancel()
    print("📴 Background workers spun down cleanly.")


# Instantiate main app with integrated resource management lifespan
app = FastAPI(
    title="CitySmart Core",
    description="Headless Urban Simulation Multi-Agent Platform",
    version="1.0.0",
    lifespan=app_lifespan_handler,
)

# Register our centralized logging telemetry middleware
app.middleware("http")(log_execution_time_middleware)


# Register robust global error exception handlers to safeguard client requests
@app.exception_handler(sqlalchemy.exc.SQLAlchemyError)
async def sqlalchemy_exception_handler(
    request: Request, exc: sqlalchemy.exc.SQLAlchemyError
):
    return JSONResponse(
        status_code=503,
        content={
            "detail": "Database operational error, connection drop, or timeout occurred.",
            "error_type": "DatabaseError",
        },
    )


@app.exception_handler(GoogleAPICallError)
async def google_api_exception_handler(request: Request, exc: GoogleAPICallError):
    return JSONResponse(
        status_code=502,
        content={
            "detail": f"Upstream LLM or Agent Engine service error: {exc.message}",
            "error_type": "LLMServiceError",
        },
    )


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    return JSONResponse(
        status_code=500,
        content={
            "detail": f"Unhandled system execution error: {str(exc)}",
            "error_type": exc.__class__.__name__,
        },
    )


# Route registration tree
app.include_router(v1_router, prefix="/api/v1")


@app.get("/health", tags=["System Diagnostics"])
async def system_health_ping():
    return {
        "status": "healthy",
        "platform": "Google Cloud Architecture Stack",
        "region": "asia-northeast3 (Seoul)",
        "multi_agent_matrix": "online",
    }
