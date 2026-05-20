FROM python:3.11-slim

WORKDIR /app

# Copy dependency structures
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of your production code
COPY . .

# Expose the standard port and run Uvicorn
EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
