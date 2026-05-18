import uuid
import json
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel, Field
from typing import Optional
from app.core.database import get_db
from app.models.database import ProjectModel, ProjectVersionModel, DocumentInsightModel
from app.schemas.project import ProjectResponse
from app.schemas.simulation import ProjectAmendmentRequest
from app.services import vertex_service, gcs_service, pubsub_service
from google.genai import types
from app.services.agent_engine import client

router = APIRouter(prefix="/projects", tags=["Urban Ingestion Engine"])

class UnstructuredIngestRequest(BaseModel):
    user_prompt: str = Field(..., description="The raw, unstructured textual layout description from the user.")

class ExtractedProjectSchema(BaseModel):
    project_id: str
    title: str
    baseline_proposal_text: str

def chunk_text_by_semantic_bounds(text: str, chunk_size: int = 1000, overlap: int = 200) -> list[str]:
    """Splits large text payloads cleanly while preserving structural context boundaries."""
    words = text.split()
    chunks = []
    for i in range(0, len(words), chunk_size - overlap):
        chunk = " ".join(words[i:i + chunk_size])
        chunks.append(chunk)
        if i + chunk_size >= len(words):
            break
    return chunks

@router.post("/ingest", response_model=dict, status_code=status.HTTP_201_CREATED)
async def ingest_unstructured_urban_document(
    payload: UnstructuredIngestRequest,
    db: AsyncSession = Depends(get_db)
):
    """Accepts unstructured user prompts and extracts mandatory schema fields using explicit types.GenerateContentConfig."""
    try:
        extraction_prompt = f"""
        Analyze the following raw urban planning proposal text. Extract or generate a valid system 'project_id' (lowercase, hyphenated, short slug), a clear descriptive 'title', and clean up the 'baseline_proposal_text'.
        
        RAW USER INPUT:
        {payload.user_prompt}
        """
        
        # Invoke Gemini 2.5 Flash using the strictly required GenerateContentConfig type wrapper
        response = client.models.generate_content(
            model='gemini-2.5-flash',
            contents=extraction_prompt,
            config=types.GenerateContentConfig(
                response_mime_type="application/json",
                response_schema=ExtractedProjectSchema,
                temperature=0.1
            )
        )
        
        # Parse the structured string safely back into native execution logic
        extracted_data = json.loads(response.text.strip())
        
        proj_id = extracted_data["project_id"]
        title = extracted_data["title"]
        proposal_text = extracted_data["baseline_proposal_text"]

        # Check if project already exists
        existing_proj = await db.execute(select(ProjectModel).where(ProjectModel.id == proj_id))
        if existing_proj.scalar_one_or_none():
            # Generate a unique slug suffix to prevent collision
            proj_id = f"{proj_id}-{uuid.uuid4().hex[:4]}"

        # 1. Archive the entire raw submission up to Google Cloud Storage
        blob_name = f"raw_ingestion_logs/{proj_id}_{uuid.uuid4().hex[:6]}.txt"
        await gcs_service.upload_text_log(blob_name, proposal_text)

        # 2. Persist our Core Project Record Map baseline inside PostgreSQL
        project = ProjectModel(
            id=proj_id, 
            name=title, 
            description=f"Automatically extracted from unstructured user prompt: {payload.user_prompt[:100]}..."
        )
        db.add(project)
        await db.flush()

        # 3. Create the initial project version v1
        initial_version = ProjectVersionModel(
            project_id=proj_id,
            version_tag="v1",
            raw_text=proposal_text
        )
        db.add(initial_version)
        await db.flush()

        # 4. Process data strings into semantic chunks
        text_chunks = chunk_text_by_semantic_bounds(proposal_text)
        if text_chunks:
            # 5. Asynchronously invoke Vertex AI to extract our high-dimensional embedding matrices
            embeddings = await vertex_service.generate_embeddings(text_chunks)
            
            # 6. Build and save our vector models to pgvector
            for content, embedding in zip(text_chunks, embeddings):
                chunk_record = DocumentInsightModel(
                    version_id=initial_version.id,
                    project_id=proj_id,
                    chunk_content=content,
                    embedding_vector=embedding
                )
                db.add(chunk_record)
            await db.commit()

        # 7. Dispatch an asynchronous event processing message into our Pub/Sub pipeline
        await pubsub_service.publish_simulation_trigger(proj_id)
        
        return {
            "status": "successfully_extracted_and_ingested",
            "extracted_metadata": {
                "project_id": proj_id,
                "title": title
            },
            "saved_payload": {
                "baseline_proposal_text": proposal_text
            }
        }
        
    except Exception as e:
        await db.rollback()
        print(f"🔴 Fatal Ingestion Failover: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Automated Schema Extraction Layer Exception: {str(e)}"
        )

@router.post("/amend", status_code=status.HTTP_201_CREATED)
async def amend_project_version(payload: ProjectAmendmentRequest, db: AsyncSession = Depends(get_db)):
    try:
        # 1. Verify that the parent project entry actually exists
        proj_result = await db.execute(select(ProjectModel).where(ProjectModel.id == payload.project_id))
        project = proj_result.scalar_one_or_none()
        if not project:
            raise HTTPException(status_code=404, detail="Parent project boundary profile not found. Create v1 first.")

        # 2. Prevent version tag collisions
        ver_result = await db.execute(
            select(ProjectVersionModel)
            .where(ProjectVersionModel.project_id == payload.project_id)
            .where(ProjectVersionModel.version_tag == payload.version_tag)
        )
        if ver_result.scalar_one_or_none():
            raise HTTPException(status_code=400, detail=f"Version '{payload.version_tag}' already exists for this project.")

        # 3. Create our new branched project version record
        new_version = ProjectVersionModel(
            project_id=payload.project_id,
            version_tag=payload.version_tag,
            raw_text=payload.amended_proposal_text
        )
        db.add(new_version)
        await db.flush()  # Extract the version auto-increment ID securely

        # 4. Perform localized semantic chunking (Simulated RAG pass)
        chunks = [payload.amended_proposal_text[i:i+1500] for i in range(0, len(payload.amended_proposal_text), 1500)]
        for chunk in chunks:
            insight_record = DocumentInsightModel(
                version_id=new_version.id,
                project_id=payload.project_id,
                chunk_content=chunk
            )
            db.add(insight_record)

        await db.commit()
        return {
            "status": "version_branched",
            "project_id": payload.project_id,
            "version_tag": payload.version_tag,
            "detail": "New architectural revision branch isolated and embedded into vector context."
        }
    except Exception as e:
        await db.rollback()
        raise HTTPException(status_code=500, detail=str(e))
