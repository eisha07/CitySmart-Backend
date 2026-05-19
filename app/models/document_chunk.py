import datetime
from sqlalchemy import String, Integer, ForeignKey, DateTime, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship
from pgvector.sqlalchemy import Vector
from app.core.database import Base


class DocumentChunkModel(Base):
    __tablename__ = "document_chunks"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    project_id: Mapped[str] = mapped_column(
        String, ForeignKey("projects.id"), nullable=False
    )
    content: Mapped[str] = mapped_column(Text, nullable=False)

    # Define a 768-dimensional vector column to perfectly store Vertex AI text-embedding arrays
    embedding: Mapped[list[float]] = mapped_column(Vector(768), nullable=False)

    created_at: Mapped[datetime.datetime] = mapped_column(
        DateTime, default=datetime.datetime.utcnow
    )

    # Inverse relationship mapping back to the parent project
    project = relationship("ProjectModel", back_populates="chunks")

    @property
    def chunk_content(self) -> str:
        return self.content
