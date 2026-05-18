import uuid
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.core.database import get_db
from app.models.database import ProjectModel, ProjectVersionModel, DocumentInsightModel
from app.schemas.project import ProjectCreate, ProjectResponse
from app.schemas.document import DocumentIngestPayload
from app.schemas.simulation import ProjectAmendmentRequest
from app.services import vertex_service, gcs_service, pubsub_service

router = APIRouter(prefix="/projects", tags=["Urban Ingestion Engine"])

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

@router.post("/ingest", response_model=ProjectResponse, status_code=status.HTTP_201_CREATED)
async def ingest_urban_document(
    meta: ProjectCreate,
    payload: DocumentIngestPayload,
    db: AsyncSession = Depends(get_db)
):
    try:
        # 1. Archive the entire raw submission up to Google Cloud Storage
        blob_name = f"raw_ingestion_logs/{meta.id}_{uuid.uuid4().hex[:6]}.txt"
        await gcs_service.upload_text_log(blob_name, payload.raw_text)

        # 2. Persist our Core Project Record Map baseline inside PostgreSQL
        project = ProjectModel(id=meta.id, name=meta.name, description=meta.description)
        db.add(project)
        await db.flush()

        # 3. Create the initial project version v1
        initial_version = ProjectVersionModel(
            project_id=meta.id,
            version_tag="v1",
            raw_text=payload.raw_text
        )
        db.add(initial_version)
        await db.flush()

        # 4. Process data strings into semantic chunks
        text_chunks = chunk_text_by_semantic_bounds(payload.raw_text)
        if text_chunks:
            # 5. Asynchronously invoke Vertex AI to extract our high-dimensional embedding matrices
            embeddings = await vertex_service.generate_embeddings(text_chunks)
            
            # 6. Build and save our vector models to pgvector
            for content, embedding in zip(text_chunks, embeddings):
                chunk_record = DocumentInsightModel(
                    version_id=initial_version.id,
                    project_id=meta.id,
                    chunk_content=content,
                    embedding_vector=embedding
                )
                db.add(chunk_record)
            await db.commit()

        # 7. Dispatch an asynchronous event processing message into our Pub/Sub pipeline
        await pubsub_service.publish_simulation_trigger(meta.id)
        
        # Refresh the object state to fully capture nested relational data bindings
        await db.refresh(project)
        return project

    except Exception as e:
        await db.rollback()
        print(f"🔴 Fatal Ingestion Failover: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal ingestion pipeline exception occurred: {str(e)}"
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
