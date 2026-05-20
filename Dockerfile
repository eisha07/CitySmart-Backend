FROM python:3.11-slim

WORKDIR /app

# Copy dependency structures
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of your production code
COPY . .

# Cloud Run injects a PORT env variable (default 8080) that the container MUST
# listen on. We use shell form CMD so $PORT is expanded at runtime.
# EXPOSE is informational only; Cloud Run uses $PORT, not this value.
ENV PORT 8080
EXPOSE 8080
CMD uvicorn app.main:app --host 0.0.0.0 --port $PORT
