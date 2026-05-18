import os
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.api.v1 import v1_router
from app.services.pubsub_worker import pubsub_worker

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
    print("🛑 Server Shutdown Triggered. Closing background network streaming channels...")
    pubsub_stream.cancel()
    print("📴 Background workers spun down cleanly.")

# Instantiate main app with integrated resource management lifespan
app = FastAPI(
    title="CitySmart Core",
    description="Headless Urban Simulation Multi-Agent Platform",
    version="1.0.0",
    lifespan=app_lifespan_handler
)

# Route registration tree
app.include_router(v1_router, prefix="/api/v1")

@app.get("/health", tags=["System Diagnostics"])
async def system_health_ping():
    return {
        "status": "healthy",
        "platform": "Google Cloud Architecture Stack",
        "region": "asia-northeast3 (Seoul)",
        "multi_agent_matrix": "online"
    }
