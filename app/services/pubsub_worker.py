import os
import json
import asyncio
from google.cloud import pubsub_v1
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker
from app.models.database import SimulationResultModel
from app.services.agent_engine import simulation_engine

# Explicitly load project credentials and system parameters
GCP_PROJECT_ID = "sinuous-branch-411610"
SUBSCRIPTION_NAME = "simulation-trigger-sub"


class CloudPubSubWorkerLoop:
    def __init__(self):
        self._subscriber = None
        self._subscription_path = None

        # Instantiate an isolated database engine to prevent thread leakage during async background processing
        DATABASE_URL = os.getenv(
            "DATABASE_URL",
            "postgresql+asyncpg://postgres:postgres@localhost:5432/citysmart",
        )
        self.engine = create_async_engine(DATABASE_URL, echo=False)
        self.session_factory = sessionmaker(
            self.engine, class_=AsyncSession, expire_on_commit=False
        )

    @property
    def subscriber(self):
        if self._subscriber is None:
            self._subscriber = pubsub_v1.SubscriberClient()
        return self._subscriber

    @property
    def subscription_path(self):
        if self._subscription_path is None:
            self._subscription_path = self.subscriber.subscription_path(
                GCP_PROJECT_ID, SUBSCRIPTION_NAME
            )
        return self._subscription_path

    async def process_background_simulation_event(
        self, project_id: str, context_chunks: list[str]
    ):
        """Runs the complete Phase 5 multi-agent simulation asynchronously inside the background worker thread."""
        print(
            f"🚀 [Worker Engine] Starting multi-agent simulation loop for project: {project_id}"
        )
        try:
            # 1. Asynchronously call our individual citizen agent personas
            commuter_critique = await simulation_engine.simulate_agent_critique(
                "female_commuter", context_chunks
            )
            driver_critique = await simulation_engine.simulate_agent_critique(
                "qingqi_driver", context_chunks
            )

            # 2. Arbitrate perspectives using our 3rd Agent Design Catalyst synthesis logic
            synthesis = await simulation_engine.run_consensus_negotiation(
                commuter_critique, driver_critique
            )

            # 3. Securely write the analytics metrics record to PostgreSQL via an isolated session context block
            async with self.session_factory() as db_session:
                sim_record = SimulationResultModel(
                    project_id=project_id,
                    metric_type="pubsub_async_synthesis",
                    payload=synthesis,
                )
                db_session.add(sim_record)
                await db_session.commit()
            print(
                f"✅ [Worker Engine] Simulation successfully processed and saved for project: {project_id}"
            )

        except Exception as e:
            print(
                f"🔴 [Worker Engine] Background processing failed for project {project_id}: {e}"
            )

    def _pubsub_callback(self, message: pubsub_v1.subscriber.message.Message):
        """Synchronous message handler received from GCP Event Broker."""
        try:
            data = json.loads(message.data.decode("utf-8"))
            project_id = data.get("project_id")
            context_chunks = data.get("context_chunks", [])

            # Safely pass the synchronous thread block over to our active AsyncIO background loop
            loop = asyncio.get_event_loop()
            if loop.is_running():
                asyncio.ensure_future(
                    self.process_background_simulation_event(project_id, context_chunks)
                )
            else:
                loop.run_until_complete(
                    self.process_background_simulation_event(project_id, context_chunks)
                )

            # Acknowledge message delivery to clear it securely from the cloud queue
            message.ack()
        except Exception as e:
            print(f"⚠️ [Worker Engine] Error handling incoming Pub/Sub envelope: {e}")
            # Nack forces the broker to redeliver the message for a second processing attempt
            message.nack()

    def start_listening(self):
        """Starts a non-blocking streaming pull background worker thread listening to the GCP subscription path."""
        print(
            f"📡 [Worker Engine] Streaming event pull initialized on {self.subscription_path}..."
        )
        streaming_pull_future = self.subscriber.subscribe(
            self.subscription_path, callback=self._pubsub_callback
        )
        return streaming_pull_future


# Instantiate singleton worker object
pubsub_worker = CloudPubSubWorkerLoop()
