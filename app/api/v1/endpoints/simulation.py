from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.core.database import get_db
from app.models.database import ProjectModel, ProjectVersionModel, DocumentInsightModel
from app.schemas.simulation import (
    SimulationStateResponse,
    CitizenPersona,
    FeasibilityScores,
    LiveTickerMessage,
    BlueprintRevision,
    CitizenPersonaProfile,
    ChatInterrogationRequest,
    ChatInterrogationResponse,
    ConceptRenderRequest,
    ConceptRenderResponse,
)
from app.services.agent_engine import simulation_engine, client
from google.genai import types
import random
import os
import json

router = APIRouter(prefix="/simulation", tags=["Agent Simulation Core"])


def generate_enhanced_friction_telemetry(project_id: str) -> dict:
    base_lat, base_lng = (
        (33.7077, 73.0551) if "islamabad" in project_id.lower() else (31.5424, 74.3462)
    )
    features = []

    # 1. Generate "Proposed Structures" (Geometric Boxes as LineStrings)
    for idx in range(2):
        s_lng, s_lat = base_lng + random.uniform(-0.002, 0.002), base_lat + random.uniform(-0.002, 0.002)
        offset = 0.001
        box_coords = [
            [s_lng, s_lat],
            [s_lng + offset, s_lat],
            [s_lng + offset, s_lat + offset],
            [s_lng, s_lat + offset],
            [s_lng, s_lat] # Close the box
        ]
        features.append({
            "type": "Feature",
            "geometry": {"type": "LineString", "coordinates": box_coords},
            "properties": {
                "agent_id": f"structure_{idx}",
                "profile": "proposed_infrastructure",
                "friction_intensity": 0.0,
            }
        })

    # 2. Generate Agent Movement Paths (Lines)
    for idx in range(5):
        coords = [[base_lng + random.uniform(-0.005, 0.005), base_lat + random.uniform(-0.005, 0.005)] for _ in range(3)]
        features.append({
            "type": "Feature",
            "geometry": {"type": "LineString", "coordinates": coords},
            "properties": {
                "agent_id": f"path_{idx}",
                "profile": "female_commuter" if idx % 2 == 0 else "qingqi_driver",
                "friction_intensity": round(random.uniform(0.5, 2.0), 2),
            }
        })

    # 3. Generate Agent Pins (Points)
    for idx in range(4):
        p_coords = [[base_lng + random.uniform(-0.003, 0.003), base_lat + random.uniform(-0.003, 0.003)]]
        features.append({
            "type": "Feature",
            "geometry": {"type": "Point", "coordinates": p_coords},
            "properties": {
                "agent_id": f"pin_{idx}",
                "profile": "citizen_observer",
                "friction_intensity": round(random.uniform(1.0, 1.5), 2),
            }
        })

    return {"type": "FeatureCollection", "features": features}


@router.get("/{project_id}/state", response_model=SimulationStateResponse)
async def get_simulation_state(project_id: str, db: AsyncSession = Depends(get_db)):
    try:
        version_result = await db.execute(
            select(ProjectVersionModel)
            .where(ProjectVersionModel.project_id == project_id)
            .order_by(ProjectVersionModel.id.asc())
        )
        versions = version_result.scalars().all()
        if not versions:
            raise HTTPException(status_code=404, detail="Project not found.")

        baseline_text = versions[0].raw_text
        amendment_text = versions[-1].raw_text if len(versions) > 1 else "No amendments."

        simulation_prompt = f"Analyze urban impact for project: {project_id}. Baseline: {baseline_text}. Amendment: {amendment_text}"

        response = client.models.generate_content(
            model="gemini-1.5-flash",
            contents=simulation_prompt,
            config=types.GenerateContentConfig(
                response_mime_type="application/json",
                response_schema=SimulationStateResponse,
                temperature=0.7,
            ),
        )

        data = json.loads(response.text.strip())
        data["status"] = "completed"
        data["summary_verdict"] = data.get("arbitrator_verdict", "No verdict generated.")
        data["scores"] = {
            "social_impact_score": float(data.get("social_feasibility_score", 50)),
            "economic_viability_score": float(data.get("economic_viability_score", 50)),
            "political_feasibility_score": float(data.get("political_acceptance_score", 50)),
        }
        data["discovered_personas"] = [{"name": p["name"], "type": p["demographic_role"], "system_instruction": p["system_instruction"]} for p in data.get("personas", [])]
        data["live_debate_ticks"] = [{"timestamp": "12:00", "agent_profile": "System", "log_level": "INFO", "message": "Simulation initialized."}]
        data["blueprint_revisions"] = [{"original_element": "Generic Road", "failure_mode_detected": "Friction", "amended_design_fix": "Dedicated Lane"}]
        data["spatial_telemetry"] = generate_enhanced_friction_telemetry(project_id)

        return SimulationStateResponse(**data)

    except Exception as e:
        error_msg = str(e)
        if "429" in error_msg:
             raise HTTPException(status_code=429, detail="Quota Exhausted")
        raise HTTPException(status_code=500, detail=error_msg)

@router.post("/chat", response_model=ChatInterrogationResponse)
async def chat_with_citizen_persona(payload: ChatInterrogationRequest):
    agent_reply = await simulation_engine.interrogate_persona(
        system_instruction=payload.persona_system_instruction,
        history=payload.chat_history,
        user_message=payload.user_message,
    )
    return ChatInterrogationResponse(reply=agent_reply)

@router.post("/render-concept", response_model=ConceptRenderResponse)
async def generate_blueprint_render(payload: ConceptRenderRequest):
    os.makedirs("app/static/assets/renders", exist_ok=True)
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
