import asyncio
from google.cloud import pubsub_v1
from app.core.config import settings


class PubSubService:
    def __init__(self):
        self.project = settings.GCP_PROJECT_ID
        self.topic_id = "simulation-trigger-topic"
        self._publisher = None

    @property
    def publisher(self):
        if self._publisher is None:
            self._publisher = pubsub_v1.PublisherClient()
        return self._publisher

    async def publish_simulation_trigger(self, project_id: str) -> str:
        """
        Asynchronously publishes an orchestration event message into our Google Cloud Pub/Sub topic.
        """
        try:
            loop = asyncio.get_running_loop()
            topic_path = self.publisher.topic_path(self.project, self.topic_id)
            data_payload = project_id.encode("utf-8")

            def _publish():
                future = self.publisher.publish(topic_path, data=data_payload)
                return future.result()

            message_id = await loop.run_in_executor(None, _publish)
            print(f"🟢 Pub/Sub Message Dispatched successfully: {message_id}")
            return message_id
        except Exception as e:
            print(f"🔴 Pub/Sub Event Dispatch Failure: {e}")
            raise RuntimeError(f"Pub/Sub broker error: {str(e)}")


pubsub_service = PubSubService()
