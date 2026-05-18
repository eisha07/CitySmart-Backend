from pydantic import BaseModel, Field

class DocumentIngestPayload(BaseModel):
    raw_text: str = Field(
        ..., 
        description="The raw unstructured policy, map notation, or city planning manual text block to process."
    )
