import logging
import sys
import time
from fastapi import Request

# Setup centralized logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(name)s | %(message)s",
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)

logger = logging.getLogger("CitySmart")

async def log_execution_time_middleware(request: Request, call_next):
    """
    Middleware that records HTTP methods, routing paths, status codes,
    and exact request execution time for telemetry.
    """
    start_time = time.time()
    response = await call_next(request)
    duration = time.time() - start_time
    logger.info(
        f"Endpoint: {request.method} {request.url.path} | Execution Time: {duration:.4f}s | Status: {response.status_code}"
    )
    return response
