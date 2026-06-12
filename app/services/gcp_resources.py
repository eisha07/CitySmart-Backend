import os
import asyncio
from google.cloud import storage
from google.cloud import secretmanager
from app.core.config import settings


class CloudStorageService:
    def __init__(self):
        self.project = settings.GCP_PROJECT_ID
        self.bucket_name = f"urban-unstructured-inputs-bucket-{self.project}"
        self._client = None
        self.local_mode = settings.ENVIRONMENT != "production"

    @property
    def client(self):
        if self._client is None:
            if settings.ENVIRONMENT == "production":
                # Ensure production does not accidentally use ANY local emulator.
                emulators = [
                    "STORAGE_EMULATOR_HOST",
                    "PUBSUB_EMULATOR_HOST",
                    "FIRESTORE_EMULATOR_HOST",
                    "FIREBASE_AUTH_EMULATOR_HOST",
                    "BIGTABLE_EMULATOR_HOST",
                ]
                for env_var in emulators:
                    os.environ.pop(env_var, None)
                self._client = storage.Client(project=self.project)
            else:
                # Local or staging might use emulators if explicitly set
                self._client = storage.Client(project=self.project)
        return self._client

    async def upload_text_log(
        self, destination_blob_name: str, text_content: str
    ) -> str:
        """
        Asynchronously streams text contents up into a designated GCS blob pathway
        by wrapping the blocking client execution in a thread pool executor.
        """
        if self.local_mode:
            local_dir = "app/static/assets/logs"
            os.makedirs(local_dir, exist_ok=True)
            safe_filename = destination_blob_name.replace("/", "_")
            local_path = os.path.join(local_dir, safe_filename)
            with open(local_path, "w", encoding="utf-8") as f:
                f.write(text_content)
            print(f"🟡 Local GCS mock saved: '{local_path}'")
            return f"/static/assets/logs/{safe_filename}"

        try:
            loop = asyncio.get_running_loop()

            def _upload():
                # Use client.bucket() lazy constructor to bypass storage.buckets.get permission checks
                bucket = self.client.bucket(self.bucket_name)
                blob = bucket.blob(destination_blob_name)
                blob.upload_from_string(text_content, content_type="text/plain")
                return blob.public_url

            public_url = await loop.run_in_executor(None, _upload)
            print(
                f"🟢 GCS Upload Success: Created storage blob '{destination_blob_name}'"
            )
            return public_url
        except Exception as e:
            print(
                f"⚠️ GCS Upload Failed: {e}. Attempting local log archive fallback..."
            )
            try:
                local_dir = "app/static/assets/logs"
                os.makedirs(local_dir, exist_ok=True)
                safe_filename = destination_blob_name.replace("/", "_")
                local_path = os.path.join(local_dir, safe_filename)
                with open(local_path, "w", encoding="utf-8") as f:
                    f.write(text_content)
                print(f"🟢 Local Log Archive Fallback Saved: '{local_path}'")
                return f"/static/assets/logs/{safe_filename}"
            except Exception as fallback_err:
                print(f"🔴 Local Fallback Failed: {fallback_err}")
                raise RuntimeError(f"GCS operational error: {str(e)}")


class SecretManagerService:
    def __init__(self):
        self.project = settings.GCP_PROJECT_ID
        self._client = None

    @property
    def client(self):
        if self._client is None:
            self._client = secretmanager.SecretManagerServiceClient()
        return self._client

    async def access_secret(self, secret_id: str, version_id: str = "latest") -> str:
        """
        Asynchronously retrieves a secret token value directly from the GCP Secret Manager vault.
        """
        try:
            loop = asyncio.get_running_loop()
            secret_path = (
                f"projects/{self.project}/secrets/{secret_id}/versions/{version_id}"
            )

            def _access():
                response = self.client.access_secret_version(
                    request={"name": secret_path}
                )
                return response.payload.data.decode("UTF-8")

            return await loop.run_in_executor(None, _access)
        except Exception as e:
            print(f"🔴 Secret Manager Access Failure on '{secret_id}': {e}")
            raise RuntimeError(f"Secret Manager operational error: {str(e)}")


# Instantiate reusable singletons for dependency application access points
gcs_service = CloudStorageService()
secret_service = SecretManagerService()
