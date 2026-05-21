FROM python:3.12-slim

WORKDIR /app

# Copy dependency structures
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of your production code
COPY . .

EXPOSE 8000

# Run Uvicorn on 0.0.0.0:8000 inside the container
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
