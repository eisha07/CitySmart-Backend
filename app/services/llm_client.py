import os
from google import genai
from app.core.config import settings

class LazyGenAIClient:
    def __init__(self):
        self._client = None

    @property
    def client(self):
        if self._client is None:
            api_key = settings.GEMINI_API_KEY or os.environ.get("GEMINI_API_KEY")
            if api_key:
                self._client = genai.Client(api_key=api_key)
            else:
                self._client = genai.Client(
                    vertexai=True,
                    project=settings.GCP_PROJECT_ID,
                    location=settings.GCP_REGION,
                )
        return self._client

    @property
    def models(self):
        return self.client.models

client = LazyGenAIClient()
vertex_client = None
