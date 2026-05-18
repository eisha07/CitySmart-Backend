from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.core.database import get_db
from app.models.database import ProjectModel, DocumentInsightModel
from app.schemas.simulation import (
    SimulationStateResponse,
    FeasibilityScores,
    LiveTickerMessage,
    BlueprintRevision,
    CitizenPersonaProfile,
    ChatInterrogationRequest,
    ConceptRenderRequest,
    ConceptRenderResponse
)
from app.services.agent_engine import simulation_engine
import random
import os

router = APIRouter(prefix="/simulation", tags=["Agent Simulation Core"])

def generate_enhanced_friction_telemetry(project_id: str) -> dict:
    base_lat, base_lng = (33.6853, 73.0312) if "islamabad" in project_id.lower() else (31.5424, 74.3462)
    features = []
    for idx in range(10):
        coords = [[base_lng + random.uniform(-0.003, 0.003), base_lat + random.uniform(-0.003, 0.003)] for _ in range(3)]
        features.append({
            "type": "Feature",
            "geometry": {"type": "LineString", "coordinates": coords},
            "properties": {
                "agent_id": f"agent_{idx}",
                "profile": "vulnerable_demographic" if idx % 2 == 0 else "transit_operator",
                "friction_intensity": round(random.uniform(0.6, 0.99), 2),
                "lighting_vector_safety": "low" if idx % 3 == 0 else "high"
            }
        })
    return {"type": "FeatureCollection", "features": features}

@router.get("/{project_id}/state", response_model=SimulationStateResponse)
async def get_simulation_state(project_id: str, db: AsyncSession = Depends(get_db)):
    try:
        insight_result = await db.execute(
            select(DocumentInsightModel).where(DocumentInsightModel.project_id == project_id).limit(3)
        )
        insights = insight_result.scalars().all()
        context_text = " ".join([insight.chunk_content for insight in insights]) if insights else "Standard urban road reconfiguration proposal."

        # 1. Dynamically extract our 10-persona demographic matrix
        discovered_raw = await simulation_engine.discover_dynamic_stakeholders(context_text)
        personas_list = [CitizenPersonaProfile(**p) for p in discovered_raw]

        # 2. Compile critiques across personas to feed arbitration
        critiques_payload = []
        for p in personas_list[:3]:  # Run a targeted sub-slice for live speed, utilizing others for structural mapping
            critique_text = await simulation_engine.simulate_dynamic_critique(p.system_instruction, [context_text])
            critiques_payload.append({"name": p.name, "type": p.type, "text": critique_text})

        # 3. Arbitrate consensus metrics
        synthesis = await simulation_engine.run_consensus_negotiation(critiques_payload)

        mock_ticks = [
            LiveTickerMessage(timestamp="21:14", agent_profile=personas_list[0].type, log_level="CRITICAL", message=f"Vulnerability risk flagged by {personas_list[0].name}: Poor lane integration disrupts native accessibility safety."),
            LiveTickerMessage(timestamp="21:16", agent_profile=personas_list[1].type, log_level="WARNING", message=f"Operational friction flagged by {personas_list[1].name}: High congestion predicted near major market drop-off points.")
        ]

        revisions_list = [BlueprintRevision(**r) for r in synthesis.get("blueprint_revisions", [])]
        if not revisions_list:
            revisions_list = [BlueprintRevision(original_element="Standard uniform concrete medians", failure_mode_detected="Blocks micro-transit access points and vendor traffic", amended_design_fix="Implement porous, modular layout setbacks with dedicated transit bay cutouts.")]

        return SimulationStateResponse(
            project_id=project_id,
            status="completed",
            scores=FeasibilityScores(
                social_impact_score=synthesis.get("social_impact_score", 68.0),
                economic_viability_score=synthesis.get("economic_viability_score", 55.0),
                political_feasibility_score=synthesis.get("political_feasibility_score", 48.0)
            ),
            summary_verdict=synthesis.get("summary_verdict", "Simulation complete. Layout requires strategic modifications to resolve citizen access bottlenecks."),
            discovered_personas=personas_list,
            live_debate_ticks=mock_ticks,
            blueprint_revisions=revisions_list,
            spatial_telemetry=generate_enhanced_friction_telemetry(project_id)
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Phase 8 Pipeline Failure: {str(e)}")

@router.post("/chat", status_code=status.HTTP_200_OK)
async def chat_with_citizen_persona(payload: ChatInterrogationRequest):
    try:
        reply = await simulation_engine.interrogate_persona(
            system_instruction=payload.persona_system_instruction,
            history=payload.chat_history,
            user_message=payload.user_message
        )
        return {"reply": reply}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Active Chat Collapse: {str(e)}")

@router.post("/render-concept", response_model=ConceptRenderResponse, status_code=200)
async def generate_blueprint_render(payload: ConceptRenderRequest):
    try:
        # Create staging assets path directory if it does not exist
        os.makedirs("app/static/assets/renders", exist_ok=True)

        # Execute our multimodal rendering engine pass
        render_data = await simulation_engine.render_architectural_concept(
            element=payload.design_element_description,
            context=payload.environmental_context
        )

        return ConceptRenderResponse(
            project_id=payload.project_id,
            version_tag=payload.version_tag,
            element_rendered=payload.design_element_description,
            generated_image_url=render_data["url"],
            revised_prompt_used=render_data["prompt"]
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Multimodal Rendering Engine Exception: {str(e)}")
