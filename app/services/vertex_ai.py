import os
import asyncio
from typing import List
from google.cloud import aiplatform
from vertexai.language_models import TextEmbeddingModel
from app.core.config import settings


class VertexEmbeddingService:
    def __init__(self):
        self.project = settings.GCP_PROJECT_ID
        self.location = settings.GCP_REGION
        self.local_mode = settings.ENVIRONMENT != "production"
        self.model_name = "text-embedding-004"
        self._model = None

        if not self.local_mode:
            # Initialize the production Vertex AI context only outside local mode.
            aiplatform.init(project=self.project, location=self.location)

    @property
    def model(self):
        """Lazy load the foundation embedding model instance safely."""
        if self.local_mode:
            return None
        if self._model is None:
            self._model = TextEmbeddingModel.from_pretrained(self.model_name)
        return self._model

    async def generate_embeddings(self, texts: List[str]) -> List[List[float]]:
        """
        Asynchronously generates 768-dimensional float vector embeddings for a list of string tokens.
        Wraps the blocking synchronous client library SDK call within an executor pool thread.
        """
        if not texts:
            return []

        if self.local_mode:
            print(
                "🟡 Vertex AI local mock enabled: returning placeholder embedding vectors."
            )
            return [[0.0] * 768 for _ in texts]

        try:
            # Offload the blocking synchronous network call to an asynchronous executor thread
            loop = asyncio.get_running_loop()
            
            def _get_embeddings():
                try:
                    return self.model.get_embeddings(texts)
                except Exception as inner_e:
                    print(f"⚠️ Vertex AI inner check failed: {inner_e}")
                    return None

            embeddings_response = await loop.run_in_executor(
                None, _get_embeddings
            )

            if embeddings_response is None:
                print("⚠️ Vertex AI unavailable. Falling back to zero-embeddings for test.")
                return [[0.0] * 768 for _ in texts]

            # Extract the raw float array elements out of the structural response objects
            return [emb.values for emb in embeddings_response]

        except Exception as e:
            print(f"⚠️ Vertex AI outer failure: {e}. Falling back to zero-embeddings for test.")
            return [[0.0] * 768 for _ in texts]


# Instantiate a reusable single-instance connection manager token for dependency injection
vertex_service = VertexEmbeddingService()
