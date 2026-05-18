import datetime
from typing import Optional, List
from pydantic import BaseModel, Field, ConfigDict

# Schema for incoming request data when creating a new project simulation profile
class ProjectCreate(BaseModel):
    id: str = Field(
        ..., 
        description="Unique alpha-numeric identifier for the urban project (e.g., 'karachi-clifton-01')"
    )
    name: str = Field(..., max_length=100, description="Human-readable name of the simulation target region")
    description: Optional[str] = Field(None, description="Optional high-level brief of the urban analysis goals")

# Schema for returning document chunk metadata profiles safely over endpoints
class DocumentChunkResponse(BaseModel):
    id: int
    content: str
    created_at: datetime.datetime

    # Enable ORM compatibility so Pydantic can read attributes directly off SQLAlchemy model objects
    model_config = ConfigDict(from_attributes=True)

# Schema for data outgoing through GET endpoints, including nested data relationships
class ProjectResponse(BaseModel):
    id: str
    name: str
    description: Optional[str]
    created_at: datetime.datetime
    chunks: List[DocumentChunkResponse] = []

    model_config = ConfigDict(from_attributes=True)
