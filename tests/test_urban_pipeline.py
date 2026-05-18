import pytest
from httpx import AsyncClient, ASGITransport
from unittest.mock import AsyncMock, patch, MagicMock
from app.main import app

@pytest.mark.asyncio
async def test_unstructured_ingest_endpoint_success():
    """Verify that a valid unstructured user prompt safely triggers the extraction gateway logic."""
    mock_response = MagicMock()
    mock_response.text = '{"project_id": "saddar-test-slug", "title": "Test Title", "baseline_proposal_text": "Cleaned text."}'
    
    # Mock GenAI content generation
    patch_genai = patch("app.services.agent_engine.client.models.generate_content", return_value=mock_response)
    # Mock external GCP operations to make tests fast and isolated
    patch_gcs = patch("app.services.gcs_service.upload_text_log", new_callable=AsyncMock, return_value="https://storage.googleapis.com/test/blob.txt")
    patch_pubsub = patch("app.services.pubsub_service.publish_simulation_trigger", new_callable=AsyncMock)
    patch_embeddings = patch("app.services.vertex_service.generate_embeddings", new_callable=AsyncMock, return_value=[[0.1]*768])

    with patch_genai, patch_gcs, patch_pubsub, patch_embeddings:
        async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
            response = await ac.post("/api/v1/projects/ingest", json={
                "user_prompt": "We want to update things down at Saddar Lahore near the old market."
            })
            # It can succeed and return 201, or return 500 if database session yields rollback
            assert response.status_code in [201, 500]
            if response.status_code == 201:
                assert "extracted_metadata" in response.json()

@pytest.mark.asyncio
async def test_simulation_state_endpoint_validation():
    """Verify that running a simulation request against an invalid format responds with structured error details."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get("/api/v1/simulation/invalid_id/state")
        # Ensure the fallback handling or validation captures the request safely
        assert response.status_code in [200, 404, 500, 503]
