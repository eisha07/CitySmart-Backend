from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional


# ── Gemini-safe schema ──────────────────────────────────────────────────────
# Contains ONLY primitive / fully-typed fields.
# Dict[str, Any] fields are EXCLUDED because Pydantic emits `additionalProperties`
# in their JSON schema, which the Gemini API rejects with INVALID_ARGUMENT.
# We enrich the response with those fields manually in the endpoint after parsing.


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


class CitizenPersona(BaseModel):
    name: str
    demographic_role: str
    sentiment_score: int = Field(
        ..., description="0 to 100 score of how much they favor the project"
    )
    system_instruction: str = Field(
        ...,
        description="First-person prompt detailing their life, concerns, and stance for subsequent chat engagement",
    )


class GeminiSimulationSchema(BaseModel):
    """Lean schema passed to Gemini response_schema. No Dict/Any fields allowed."""

    project_id: str
    personas: List[CitizenPersona]
    social_feasibility_score: int
    economic_viability_score: int
    political_acceptance_score: int
    arbitrator_verdict: str


class SimulationStateResponse(BaseModel):
    project_id: str
    personas: List[CitizenPersona]
    social_feasibility_score: int
    economic_viability_score: int
    political_acceptance_score: int
    arbitrator_verdict: str

    # Backward compatibility properties (optional/defaulted)
    status: str = "completed"
    scores: Optional[FeasibilityScores] = None
    summary_verdict: Optional[str] = None
    discovered_personas: Optional[List[CitizenPersonaProfile]] = None
    live_debate_ticks: Optional[List[LiveTickerMessage]] = None
    blueprint_revisions: Optional[List[BlueprintRevision]] = None
    spatial_telemetry: Optional[Dict[str, Any]] = None


class ChatInterrogationRequest(BaseModel):
    project_id: str = Field(
        ..., description="The unique identifier of the target project domain."
    )
    persona_system_instruction: str = Field(
        ...,
        description="The explicit anthropomorphic system context generated for this specific citizen.",
    )
    user_message: str = Field(
        ...,
        description="The active engineering query or adjustment pitched by the city official.",
    )
    chat_history: List[Dict[str, str]] = Field(
        default=[],
        description="The multi-turn conversational log array structured as [{'role': 'user'|'model', 'content': '...'}]",
    )


class ChatInterrogationResponse(BaseModel):
    reply: str = Field(
        ...,
        description="The first-person qualitative pushback or agreement from the citizen persona.",
    )


class ProjectAmendmentRequest(BaseModel):
    project_id: str
    version_tag: str  # e.g., "v2-amended"
    amended_proposal_text: str


class ConceptRenderRequest(BaseModel):
    project_id: str
    version_tag: str
    design_element_description: str
    environmental_context: Optional[str] = Field(
        default="Daytime, clean modern architecture, South Asian metropolitan context"
    )


class ConceptRenderResponse(BaseModel):
    project_id: str
    version_tag: str
    element_rendered: str
    generated_image_url: str
    revised_prompt_used: str
