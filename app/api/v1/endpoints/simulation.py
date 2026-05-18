import random
from fastapi import APIRouter, HTTPException, status
from app.schemas.simulation import SimulationRunRequest, SimulationStateResponse, FeasibilityScores

router = APIRouter(prefix="/simulation", tags=["Agent Simulation Core"])

def generate_mock_spatial_telemetry(project_id: str) -> dict:
    """Generates valid RFC 7946 GeoJSON grids tracking simulated urban agents with localized risk variables."""
    # Determine geographic centroid based on project ID string patterns
    if "islamabad" in project_id.lower():
        base_lat, base_lng = 33.6853, 73.0312  # G-9 Islamabad
    else:
        base_lat, base_lng = 31.5424, 74.3462  # Saddar Lahore

    features = []
    
    # Generate 5 sample mock agent path trajectories (LineStrings)
    for agent_idx in range(5):
        agent_type = "female_commuter" if agent_idx % 2 == 0 else "qingqi_driver"
        coordinates = []
        
        # Step across localized spatial offsets
        curr_lat, curr_lng = base_lat, base_lng
        for step in range(4):
            curr_lat += random.uniform(-0.002, 0.002)
            curr_lng += random.uniform(-0.002, 0.002)
            coordinates.append([curr_lng, curr_lat]) # GeoJSON uses [longitude, latitude] order

        features.append({
            "type": "Feature",
            "geometry": {
                "type": "LineString",
                "coordinates": coordinates
            },
            "properties": {
                "agent_id": f"agent_{agent_idx}",
                "profile": agent_type,
                "pedestrian_density": round(random.uniform(10.0, 95.0), 2),
                "collision_risk": round(random.uniform(0.1, 0.85), 2),
                "shortcut_taken": random.choice([True, False]),
                "lighting_vector_safety": "high" if agent_type == "female_commuter" and random.choice([True, False]) else "low"
            }
        })

    return {
        "type": "FeatureCollection",
        "features": features
    }

@router.post("/run", status_code=status.HTTP_202_ACCEPTED)
async def trigger_agent_simulation(payload: SimulationRunRequest):
    return {"status": "queued", "project_id": payload.project_id, "detail": "Multi-agent engine simulation threads initiated."}

@router.get("/{project_id}/state", response_model=SimulationStateResponse)
async def get_simulation_state(project_id: str):
    # Instantly return structural valid telemetry data shapes so the mobile UI is unblocked
    return SimulationStateResponse(
        project_id=project_id,
        status="completed",
        current_step=100,
        total_steps=100,
        scores=FeasibilityScores(
            social_impact_score=78.5,
            economic_viability_score=62.0,
            political_feasibility_score=45.8
        ),
        spatial_telemetry=generate_mock_spatial_telemetry(project_id)
    )
