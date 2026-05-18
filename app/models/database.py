from app.core.database import Base
from app.models.project import ProjectModel
from app.models.document_chunk import DocumentChunkModel as DocumentInsightModel
from sqlalchemy import String, Integer, ForeignKey, DateTime, Text, JSON
from sqlalchemy.orm import Mapped, mapped_column, relationship
import datetime

class SimulationResultModel(Base):
    __tablename__ = "simulation_results"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    project_id: Mapped[str] = mapped_column(String, ForeignKey("projects.id"), nullable=False)
    metric_type: Mapped[str] = mapped_column(String, nullable=False)
    payload: Mapped[dict] = mapped_column(JSON, nullable=False)
    created_at: Mapped[datetime.datetime] = mapped_column(
        DateTime, default=datetime.datetime.utcnow
    )

    project = relationship("ProjectModel")
