from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.core.database import get_db
from app.models.database import ProjectModel, ProjectVersionModel, DocumentInsightModel
from app.schemas.simulation import (
    GeminiSimulationSchema,
    SimulationStateResponse,
    CitizenPersona,
    FeasibilityScores,
    LiveTickerMessage,
    BlueprintRevision,
    CitizenPersonaProfile,
    PersonaMetadata,
    ChatInterrogationRequest,
    ChatInterrogationResponse,
    ConceptRenderRequest,
    ConceptRenderResponse,
)
from app.services.agent_engine import simulation_engine, client, parse_summary_points
from app.services.persona_registry import lookup_persona_metadata
from google.genai import types
import random
import os
import json

router = APIRouter(prefix="/simulation", tags=["Agent Simulation Core"])


def generate_enhanced_friction_telemetry(project_id: str) -> dict:
    base_lat, base_lng = (
        (33.6853, 73.0312) if "islamabad" in project_id.lower() else (31.5424, 74.3462)
    )
    features = []
    for idx in range(10):
        coords = [
            [
                base_lng + random.uniform(-0.003, 0.003),
                base_lat + random.uniform(-0.003, 0.003),
            ]
            for _ in range(3)
        ]
        features.append(
            {
                "type": "Feature",
                "geometry": {"type": "LineString", "coordinates": coords},
                "properties": {
                    "agent_id": f"agent_{idx}",
                    "profile": (
                        "vulnerable_demographic" if idx % 2 == 0 else "transit_operator"
                    ),
                    "friction_intensity": round(random.uniform(0.6, 0.99), 2),
                    "lighting_vector_safety": "low" if idx % 3 == 0 else "high",
                },
            }
        )
    return {"type": "FeatureCollection", "features": features}


@router.get("/{project_id}/state", response_model=SimulationStateResponse)
async def get_simulation_state(project_id: str, db: AsyncSession = Depends(get_db)):
    try:
        # 1. Fetch baseline and amendment context strings from Cloud SQL
        version_result = await db.execute(
            select(ProjectVersionModel)
            .where(ProjectVersionModel.project_id == project_id)
            .order_by(ProjectVersionModel.id.asc())
        )
        versions = version_result.scalars().all()
        if not versions:
            raise HTTPException(
                status_code=404,
                detail="Parent project boundary profile not found. Create v1 first.",
            )

        baseline_text = versions[0].raw_text
        amendment_text = (
            versions[-1].raw_text
            if len(versions) > 1
            else "No active amendments or revisions."
        )

        # 2. Retrieve relevant text chunks using pgvector storage context
        insight_result = await db.execute(
            select(DocumentInsightModel)
            .where(DocumentInsightModel.project_id == project_id)
            .limit(5)
        )
        insights = insight_result.scalars().all()
        vector_context = (
            " ".join([insight.chunk_content for insight in insights])
            if insights
            else baseline_text
        )

        # 3. Build the prompt for the multi-agent debate simulation and feasibility scorecard
        simulation_prompt = f"""
        You are the CitySmart Core Urban Simulation Multi-Agent Matrix.
        Evaluate the following project under the Saddar Lahore urban planning context.
        
        PROJECT ID: {project_id}
        BASELINE DESIGN PROPOSAL:
        {baseline_text}
        
        ACTIVE AMENDMENT / BLUEPRINT DESIGN FIX:
        {amendment_text}
        
        VECTOR CONTEXT CHUNKS:
        {vector_context}
        
        INSTRUCTIONS:
        1. Generate exactly 10 highly diverse, hyper-localized citizen personas (e.g. rickshaw drivers, local Saddar shopkeepers, pedestrian shoppers, elderly residents, female commuters, students, traffic wardens). 
           For each citizen, generate a unique descriptive 'name', their specific 'demographic_role', a quantitative 'sentiment_score' (0-100) detailing how much they support the project revision, and an anthropomorphic 'system_instruction' written in first-person (e.g. 'I am Muhammad, a 45-year-old rickshaw driver in Saddar...') detailing their day-to-day commute challenges, concerns, and stance on the project.
        2. Evaluate overall project feasibility indices (0 to 100) for:
           - 'social_feasibility_score'
           - 'economic_viability_score'
           - 'political_acceptance_score'
        3. Act as the 'Urban Mediator Agent'. Synthesize all persona viewpoints into a structured JSON output — NOT prose paragraphs.
           - 'arbitrator_verdict': A single concise headline sentence that captures the most critical systemic finding.
           - 'summary_points': A JSON array of exactly 5 to 7 distinct strings. Each string must be one standalone,
             actionable policy insight or community trade-off. Do NOT write sentences that flow into each other.
             Each point must be independently readable as a bullet item in a mobile UI list view.
             Example format: ["Point about pedestrian safety.", "Point about rickshaw lane displacement.", ...]
        """

        # Invoke Gemini 2.5 Flash with the lean GeminiSimulationSchema.
        # We CANNOT pass SimulationStateResponse here because it contains
        # Dict[str, Any] fields (spatial_telemetry) that Pydantic serialises
        # with `additionalProperties`, which the Gemini API rejects (INVALID_ARGUMENT).
        # We enrich the parsed response with those fields manually below.
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=simulation_prompt,
            config=types.GenerateContentConfig(
                response_mime_type="application/json",
                response_schema=GeminiSimulationSchema,
                temperature=0.7,
            ),
        )

        data = json.loads(response.text.strip())

        # ───────────────────────────────────────────────────────────────────────────────
        # 4a. Extract summary_points (Prompt 1)
        # Primary: use the structured list already embedded in the Gemini JSON response.
        # Fallback: apply parse_summary_points() on the arbitrator_verdict string so a
        # 500 error is never thrown even if Gemini degrades to prose output.
        # ───────────────────────────────────────────────────────────────────────────────
        raw_summary_points = data.get("summary_points", [])
        if isinstance(raw_summary_points, list) and len(raw_summary_points) >= 2:
            # LLM returned a valid array — clean whitespace only.
            summary_points: list[str] = [
                str(p).strip() for p in raw_summary_points if str(p).strip()
            ]
        else:
            # Fallback: parse the arbitrator_verdict prose into bullet points.
            verdict_text: str = data.get("arbitrator_verdict", "")
            summary_points = parse_summary_points(verdict_text)

        data["summary_points"] = summary_points

        # ───────────────────────────────────────────────────────────────────────────────
        # 4b. Build active_agents from the static PersonaRegistry (Prompt 2)
        # Each dynamically-generated LLM persona is fuzzy-matched to a canonical
        # PersonaMetadata profile so the Compose (ⓘ) modal always gets structured,
        # non-hallucinated characteristics data — no extra LLM round-trip needed.
        # ───────────────────────────────────────────────────────────────────────────────
        active_agents: list[PersonaMetadata] = [
            lookup_persona_metadata(
                name=p.get("name", "Unknown Agent"),
                role=p.get("demographic_role", ""),
            )
            for p in data.get("personas", [])
        ]
        data["active_agents"] = [agent.model_dump() for agent in active_agents]

        # 4c. Inject backward-compatibility properties for dashboard view integrity
        data["status"] = "completed"
        data["summary_verdict"] = data["arbitrator_verdict"]
        data["scores"] = {
            "social_impact_score": float(data["social_feasibility_score"]),
            "economic_viability_score": float(data["economic_viability_score"]),
            "political_feasibility_score": float(data["political_acceptance_score"]),
        }
        data["discovered_personas"] = [
            {
                "name": p["name"],
                "type": p["demographic_role"],
                "system_instruction": p["system_instruction"],
            }
            for p in data["personas"]
        ]

        # Populate interactive live debate logs dynamically based on the generated personas
        data["live_debate_ticks"] = [
            {
                "timestamp": "21:14",
                "agent_profile": data["personas"][0]["demographic_role"],
                "log_level": (
                    "CRITICAL"
                    if data["personas"][0]["sentiment_score"] < 50
                    else "INFO"
                ),
                "message": f"Critical stance from {data['personas'][0]['name']}: Stance evaluated at {data['personas'][0]['sentiment_score']}% favorability. Issues flagged: accessibility and local traffic integration.",
            },
            {
                "timestamp": "21:16",
                "agent_profile": data["personas"][1]["demographic_role"],
                "log_level": (
                    "WARNING" if data["personas"][1]["sentiment_score"] < 60 else "INFO"
                ),
                "message": f"Feedback log from {data['personas'][1]['name']}: Stance evaluated at {data['personas'][1]['sentiment_score']}% favorability. Highlights commercial or vendor placement trade-offs.",
            },
        ]

        # Populate logical design catalyst blueprint revisions
        data["blueprint_revisions"] = [
            {
                "original_element": "Standard rigid road lane boundaries and layout medians",
                "failure_mode_detected": "Neglects micro-mobility rickshaw lanes and merchant pedestrian access boundaries",
                "amended_design_fix": "Implement modular layout setbacks with dedicated rickshaw pick-up bays and pedestrianized walking paths as synthesized in the arbitrator verdict.",
            }
        ]
        data["spatial_telemetry"] = generate_enhanced_friction_telemetry(project_id)

        # Re-validate the fully enriched payload against the Pydantic schema before returning
        return SimulationStateResponse(**data)

    except Exception as e:
        print(f"🔴 Fatal Simulation Failure: {e}")
        raise HTTPException(
            status_code=500, detail=f"Simulation Pipeline Failure: {str(e)}"
        )


@router.post(
    "/chat", response_model=ChatInterrogationResponse, status_code=status.HTTP_200_OK
)
async def chat_with_citizen_persona(payload: ChatInterrogationRequest):
    """Allows a user to pitch direct layout changes to a specific persona and get immediate qualitative pushback."""
    try:
        # Pass the payload directly down to the stateful service layer execution track
        agent_reply = await simulation_engine.interrogate_persona(
            system_instruction=payload.persona_system_instruction,
            history=payload.chat_history,
            user_message=payload.user_message,
        )

        return ChatInterrogationResponse(reply=agent_reply)

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Interactive Chat Session Failure: {str(e)}",
        )


@router.post("/render-concept", response_model=ConceptRenderResponse, status_code=200)
async def generate_blueprint_render(payload: ConceptRenderRequest):
    try:
        # Create staging assets path directory if it does not exist
        os.makedirs("app/static/assets/renders", exist_ok=True)

        # Execute our multimodal rendering engine pass
        render_data = await simulation_engine.render_architectural_concept(
            element=payload.design_element_description,
            context=payload.environmental_context,
        )

        return ConceptRenderResponse(
            project_id=payload.project_id,
            version_tag=payload.version_tag,
            element_rendered=payload.design_element_description,
            generated_image_url=render_data["url"],
            revised_prompt_used=render_data["prompt"],
        )
    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"Multimodal Rendering Engine Exception: {str(e)}"
        )
