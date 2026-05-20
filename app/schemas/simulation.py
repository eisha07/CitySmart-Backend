from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional


# ── Persona Metadata ────────────────────────────────────────────────────────
# Rich profile object returned alongside each active agent so the Jetpack
# Compose frontend can populate the (ⓘ) icon popup modal.
class PersonaMetadata(BaseModel):
    name: str = Field(..., description="Display name of the agent persona.")
    icon_tag: str = Field(
        default="👤",
        description="Emoji or short icon slug rendered in the agent list card.",
    )
    short_description: str = Field(
        ..., description="One-line subtitle shown beneath the persona name."
    )
    characteristics: List[str] = Field(
        ...,
        description=(
            "Ordered list of core traits, priorities, and behavioral tendencies "
            "used to populate the info-modal bullet view."
        ),
    )


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
    """
    Lean schema passed to Gemini response_schema. No Dict/Any fields allowed.

    IMPORTANT: List[str] is safe here (serialises as a plain JSON array).
    Dict[str, Any] fields are EXCLUDED — they emit `additionalProperties`
    which the Gemini API rejects with INVALID_ARGUMENT.
    """

    project_id: str
    personas: List[CitizenPersona]
    social_feasibility_score: int
    economic_viability_score: int
    political_acceptance_score: int
    # Human-readable headline kept for backward compat / logging
    arbitrator_verdict: str
    # NEW: structured mediator output — each element is one actionable bullet
    summary_points: List[str] = Field(
        default_factory=list,
        description=(
            "Ordered array of distinct, actionable policy synthesis points produced "
            "by the Mediator Agent. Maps directly to the Compose bulleted list view."
        ),
    )


class SimulationStateResponse(BaseModel):
    project_id: str
    personas: List[CitizenPersona]
    social_feasibility_score: int
    economic_viability_score: int
    political_acceptance_score: int
    arbitrator_verdict: str

    # ── Mediator structured output (Prompt 1) ────────────────────────────
    # Each string is a single bullet point for the Compose LazyColumn view.
    summary_points: List[str] = Field(
        default_factory=list,
        description="Distinct, actionable synthesis points from the Mediator Agent.",
    )

    # ── Rich persona metadata (Prompt 2) ─────────────────────────────────
    # Populated from the static PersonaRegistry; powers the (ⓘ) modal.
    active_agents: Optional[List[PersonaMetadata]] = Field(
        default=None,
        description="Full characteristic profiles for each active persona agent.",
    )

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
