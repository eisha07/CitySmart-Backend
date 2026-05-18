import os
from google.cloud import pubsub_v1
from google.cloud import storage
from google.cloud import secretmanager

PROJECT_ID = "sinuous-branch-411610"
TOPIC_ID = "simulation-trigger-topic"
BUCKET_ID = "urban-unstructured-inputs-bucket-sinuous-branch-411610"
SECRET_ID = "DATABASE_URL"

print("=== Starting GCP Live Infrastructure Diagnostics ===")

# 1. Test Cloud Storage Access
try:
    storage_client = storage.Client(project=PROJECT_ID)
    bucket = storage_client.get_bucket(BUCKET_ID)
    print(f"Cloud Storage: Successfully connected to bucket '{BUCKET_ID}'")
except Exception as e:
    print(f"Cloud Storage Error: {e}")

# 2. Test Pub/Sub Access
try:
    publisher = pubsub_v1.PublisherClient()
    topic_path = publisher.topic_path(PROJECT_ID, TOPIC_ID)
    topic = publisher.get_topic(request={"topic": topic_path})
    print(f"Pub/Sub: Successfully verified topic '{TOPIC_ID}'")
except Exception as e:
    print(f"Pub/Sub Error: {e}")

# 3. Test Secret Manager Access
try:
    secret_client = secretmanager.SecretManagerServiceClient()
    secret_path = secret_client.secret_path(PROJECT_ID, SECRET_ID)
    secret = secret_client.get_secret(request={"name": secret_path})
    print(f"Secret Manager: Successfully located secret '{SECRET_ID}' metadata")
except Exception as e:
    print(f"Secret Manager Error: {e}")

print("=====================================================")
