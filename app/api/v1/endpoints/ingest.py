import uuid
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.models.project import ProjectModel
from app.models.document_chunk import DocumentChunkModel
from app.schemas.project import ProjectCreate, ProjectResponse
from app.schemas.document import DocumentIngestPayload
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
        await db.commit()

        # 3. Process data strings into semantic chunks
        text_chunks = chunk_text_by_semantic_bounds(payload.raw_text)
        if text_chunks:
            # 4. Asynchronously invoke Vertex AI to extract our high-dimensional embedding matrices
            embeddings = await vertex_service.generate_embeddings(text_chunks)
            
            # 5. Build and save our vector models to pgvector
            for content, embedding in zip(text_chunks, embeddings):
                chunk_record = DocumentChunkModel(
                    project_id=meta.id,
                    content=content,
                    embedding=embedding
                )
                db.add(chunk_record)
            await db.commit()

        # 6. Dispatch an asynchronous event processing message into our Pub/Sub pipeline
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
