from sqlalchemy import Column, String, Integer, Float, ForeignKey, JSON, DateTime
from sqlalchemy.orm import relationship
from datetime import datetime
from app.core.database import Base

class ProjectModel(Base):
    __tablename__ = "projects"
    
    id = Column(String, primary_key=True, index=True)
    name = Column(String, nullable=False)
    description = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    # Establish a cascading relational link to version child records
    versions = relationship("ProjectVersionModel", back_populates="project", cascade="all, delete-orphan", lazy="selectin")

    @property
    def chunks(self):
        # Find version 'v1' and return its document_insights to preserve backwards compatibility
        for v in self.versions:
            if v.version_tag == "v1":
                return v.document_insights
        return []

class ProjectVersionModel(Base):
    __tablename__ = "project_versions"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    project_id = Column(String, ForeignKey("projects.id", ondelete="CASCADE"), nullable=False)
    version_tag = Column(String, default="v1", nullable=False)  # e.g., "v1", "v2-amended", "v3-final"
    raw_text = Column(String, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    # Relationships
    project = relationship("ProjectModel", back_populates="versions")
    document_insights = relationship("DocumentInsightModel", back_populates="version", cascade="all, delete-orphan", lazy="selectin")
    simulation_results = relationship("SimulationResultModel", back_populates="version", cascade="all, delete-orphan", lazy="selectin")

class DocumentInsightModel(Base):
    __tablename__ = "document_insights"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    version_id = Column(Integer, ForeignKey("project_versions.id", ondelete="CASCADE"), nullable=False)
    project_id = Column(String, nullable=False)  # Retained for rapid indexing
    chunk_content = Column(String, nullable=False)
    embedding_vector = Column(JSON, nullable=True)  # Stored array format or vector mapping tracking line
    
    version = relationship("ProjectVersionModel", back_populates="document_insights")

    @property
    def content(self) -> str:
        return self.chunk_content

    @property
    def created_at(self) -> datetime:
        return self.version.created_at if self.version else datetime.utcnow()

class SimulationResultModel(Base):
    __tablename__ = "simulation_results"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    version_id = Column(Integer, ForeignKey("project_versions.id", ondelete="CASCADE"), nullable=False)
    project_id = Column(String, nullable=False)  # Retained for flat queries
    metric_type = Column(String, default="gemini_synthesis")
    payload = Column(JSON, nullable=False)  # Stores scores, dynamic 10-persona configurations, ticker logs, and design fixes
    created_at = Column(DateTime, default=datetime.utcnow)
    
    version = relationship("ProjectVersionModel", back_populates="simulation_results")
