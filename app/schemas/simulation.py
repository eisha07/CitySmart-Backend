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
    project_id: str = Field(..., description="The unique identifier of the target project domain.")
    persona_system_instruction: str = Field(..., description="The explicit anthropomorphic system context generated for this specific citizen.")
    user_message: str = Field(..., description="The active engineering query or adjustment pitched by the city official.")
    chat_history: List[Dict[str, str]] = Field(
        default=[], 
        description="The multi-turn conversational log array structured as [{'role': 'user'|'model', 'content': '...'}]"
    )

class ChatInterrogationResponse(BaseModel):
    reply: str = Field(..., description="The first-person qualitative pushback or agreement from the citizen persona.")

class ProjectAmendmentRequest(BaseModel):
    project_id: str
    version_tag: str  # e.g., "v2-amended"
    amended_proposal_text: str

class ConceptRenderRequest(BaseModel):
    project_id: str
    version_tag: str
    design_element_description: str
    environmental_context: Optional[str] = Field(default="Daytime, clean modern architecture, South Asian metropolitan context")

class ConceptRenderResponse(BaseModel):
    project_id: str
    version_tag: str
    element_rendered: str
    generated_image_url: str
    revised_prompt_used: str
