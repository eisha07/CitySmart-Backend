from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional

class LiveTickerMessage(BaseModel):
    timestamp: str
    agent_profile: str
    log_level: str
    message: str

class BlueprintRevision(BaseModel):
    original_element: str
    failure_mode_detected: str
    amended_design_fix: str

class FeasibilityScores(BaseModel):
    social_impact_score: float
    economic_viability_score: float
    political_feasibility_score: float

class CitizenPersonaProfile(BaseModel):
    name: str
    type: str
    system_instruction: str

class SimulationStateResponse(BaseModel):
    project_id: str
    status: str
    scores: FeasibilityScores
    summary_verdict: str
    discovered_personas: List[CitizenPersonaProfile] = Field(..., description="The 10 dynamic citizen personas extracted by the LLM.")
    live_debate_ticks: List[LiveTickerMessage]
    blueprint_revisions: List[BlueprintRevision]
    spatial_telemetry: Dict[str, Any]

class ChatInterrogationRequest(BaseModel):
    project_id: str
    persona_system_instruction: str
    user_message: str
    chat_history: List[Dict[str, str]] = Field(default=[], description="List of maps following [{'role': 'user'|'model', 'content': '...'}]")
