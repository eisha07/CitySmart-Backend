from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional

class FeasibilityScores(BaseModel):
    social_impact_score: float = Field(..., description="Score tracking pedestrian safety, inclusivity, and gender-segregated access.")
    economic_viability_score: float = Field(..., description="Score weighing infrastructure cost against local utility/commercial output.")
    political_feasibility_score: float = Field(..., description="Score calculating risk of informal encroachment clashes or 'white elephant' status.")

class SimulationRunRequest(BaseModel):
    project_id: str = Field(..., example="saddar-lahore-cleanup")
    agent_count: int = Field(default=50, ge=10, le=500)
    enable_informal_nodes: bool = Field(default=True, description="Toggle tracking for Qingqi/Rickshaw congregations.")

class SimulationStateResponse(BaseModel):
    project_id: str
    status: str # e.g., 'running', 'completed'
    current_step: int
    total_steps: int
    scores: FeasibilityScores
    spatial_telemetry: Dict[str, Any] = Field(..., description="Valid RFC 7946 GeoJSON FeatureCollection containing agent paths.")
