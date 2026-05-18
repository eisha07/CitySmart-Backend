import asyncio
from typing import List
from google.cloud import aiplatform
from vertexai.language_models import TextEmbeddingModel
from app.core.config import settings


class VertexEmbeddingService:
    def __init__(self):
        self.project = settings.GCP_PROJECT_ID
        self.location = settings.GCP_REGION
        # Initialize the structural underlying Vertex AI platform context
        aiplatform.init(project=self.project, location=self.location)
        self.model_name = "text-embedding-004"
        self._model = None

    @property
    def model(self):
        """Lazy load the foundation embedding model instance safely."""
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

        try:
            # Offload the blocking synchronous network call to an asynchronous executor thread
            loop = asyncio.get_running_loop()
            embeddings_response = await loop.run_in_executor(
                None, lambda: self.model.get_embeddings(texts)
            )

            # Extract the raw float array elements out of the structural response objects
            return [emb.values for emb in embeddings_response]

        except Exception as e:
            # Fallback debugger logging for granular tracking
            print(f"🔴 Vertex AI Embedding Generation Error: {e}")
            raise RuntimeError(
                f"Failed to generate text embeddings via Vertex AI: {str(e)}"
            )


# Instantiate a reusable single-instance connection manager token for dependency injection
vertex_service = VertexEmbeddingService()
