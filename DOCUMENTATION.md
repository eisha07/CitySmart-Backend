# CitySmart Strategic Planning Portal | System Documentation & Architectural Ledger

## 1. System Architecture Diagram

Below is the comprehensive technical mapping of the CitySmart Multi-Agent Simulation Engine, showcasing data ingestion flow, vector chunk processing, Pub/Sub-driven asynchronous task queue execution, Vertex AI LLM/Vision endpoints integration, and real-time visualization delivery:

```mermaid
graph TD
    %% Ingest Flow
    Client[Client / Urban Planner] -->|1. POST /api/v1/projects/ingest| IngestRoute[Ingestion Endpoint]
    Client -->|2. POST /api/v1/projects/amend| IngestRoute
    
    %% Storage & Vector Flow
    IngestRoute -->|Save Raw/Version Text| GCS[Google Cloud Storage Logging]
    IngestRoute -->|Flush Models| DB[(PostgreSQL + pgvector)]
    IngestRoute -->|Embed Chunks| VertexEmbed[Vertex AI Text-Embedding-004]
    VertexEmbed -->|Vector Mappings| DB
    
    %% PubSub Asynchronous Path
    IngestRoute -->|3. Dispatch Simulation Event| PubSub[Google Cloud Pub/Sub Topic]
    PubSub -->|Streaming Pull| WorkerLoop[Asynchronous PubSub Worker Loop]
    
    %% Worker & Multi-Agent Matrix
    WorkerLoop -->|Retrieve Insights Context| DB
    WorkerLoop -->|4. Trigger Multi-Persona Critiques| PersonaMatrix[10-Persona Citizen Demographic Matrix]
    PersonaMatrix -->|Critiques & Personal Vulnerabilities| GeminiPro[Gemini 1.5 Pro Engine]
    GeminiPro -->|Synthesize Consensus & Revisions| Arbitrator[Urban Design Arbitrator Synthesis]
    Arbitrator -->|5. Commit Synthetic Analytics & Scores| DB
    
    %% Multimodal Image Rendering
    Client -->|6. POST /api/v1/simulation/render-concept| RenderRoute[Multimodal Concept Rendering]
    RenderRoute -->|Optimize Prompt| GeminiPro
    GeminiPro -->|Visual Directives| ImagenEngine[Vertex AI Imagen 3 Model]
    ImagenEngine -->|Generate Mockups| Assets[Static Assets Directory]
    Assets -->|Mockup URL Path| Client
    
    %% Interactive Chat Dialogue
    Client -->|7. POST /api/v1/simulation/chat| ChatRoute[Interactive Chat Endpoint]
    ChatRoute -->|Multi-turn Roleplay Session| GeminiPro
```

---

## 2. API Specification Ledger

### A. POST `/api/v1/projects/amend`
* **Purpose**: Submits a branched architectural amendment proposal to an existing project configuration without modifying parent project logs.
* **Content Type**: `application/json`
* **Request Payload (`ProjectAmendmentRequest`)**:
```json
{
  "project_id": "karachi-clifton-01",
  "version_tag": "v2-amended",
  "amended_proposal_text": "Amended layout specifications integrating a dedicated qingqi rickshaw terminal lane and pedestrian buffer zones..."
}
```
* **Response Payload**:
```json
{
  "status": "version_branched",
  "project_id": "karachi-clifton-01",
  "version_tag": "v2-amended",
  "detail": "New architectural revision branch isolated and embedded into vector context."
}
```

---

### B. GET `/api/v1/simulation/{project_id}/state`
* **Purpose**: Fetches synthesized community feedback, live debate tick logs, design catalyst architectural prescriptions, spatial telemetry features, and structural feasibility scoring matrices.
* **Content Type**: `application/json`
* **Response Payload (`SimulationStateResponse`)**:
```json
{
  "project_id": "karachi-clifton-01",
  "status": "completed",
  "scores": {
    "social_impact_score": 68.0,
    "economic_viability_score": 55.0,
    "political_feasibility_score": 48.0
  },
  "summary_verdict": "Simulation complete. Layout requires strategic modifications to resolve citizen access bottlenecks.",
  "discovered_personas": [
    {
      "name": "Zainab",
      "type": "female_commuter",
      "system_instruction": "You are Zainab, a 28-year-old formal commuter traveling daily..."
    }
  ],
  "live_debate_ticks": [
    {
      "timestamp": "21:14",
      "agent_profile": "female_commuter",
      "log_level": "CRITICAL",
      "message": "Vulnerability risk flagged by Zainab: Poor lane integration disrupts native accessibility safety."
    }
  ],
  "blueprint_revisions": [
    {
      "original_element": "Standard uniform concrete medians",
      "failure_mode_detected": "Blocks micro-transit access points and vendor traffic",
      "amended_design_fix": "Implement porous, modular layout setbacks with dedicated transit bay cutouts."
    }
  ],
  "spatial_telemetry": {
    "type": "FeatureCollection",
    "features": [
      {
        "type": "Feature",
        "geometry": {
          "type": "LineString",
          "coordinates": [[74.3432, 31.5454], [74.3444, 31.5422]]
        },
        "properties": {
          "agent_id": "agent_0",
          "profile": "vulnerable_demographic",
          "friction_intensity": 0.85,
          "lighting_vector_safety": "low"
        }
      }
    ]
  }
}
```

---

### C. POST `/api/v1/simulation/chat`
* **Purpose**: Cross-examines any of the 10 extracted citizen personas in an active, stateful multi-turn roleplay dialogue session.
* **Content Type**: `application/json`
* **Request Payload (`ChatInterrogationRequest`)**:
```json
{
  "project_id": "karachi-clifton-01",
  "persona_system_instruction": "You are Zainab, a 28-year-old formal commuter...",
  "user_message": "What if we add dedicated streetlights and security guards at the bus stop?",
  "chat_history": [
    {
      "role": "user",
      "content": "Hello. How do you feel about the proposed concrete barrier?"
    },
    {
      "role": "model",
      "content": "It completely blocks my path when crossing from the local transit stop."
    }
  ]
}
```
* **Response Payload (`ChatInterrogationResponse`)**:
```json
{
  "reply": "Dedicated streetlights and guards would significantly improve my sense of safety during late-night returns!"
}
```

---

### D. POST `/api/v1/simulation/render-concept`
* **Purpose**: Leverages Vertex AI Imagen 3 to dynamically generate 1:1 ratio high-fidelity visual concept renderings of blueprint modifications.
* **Content Type**: `application/json`
* **Request Payload (`ConceptRenderRequest`)**:
```json
{
  "project_id": "karachi-clifton-01",
  "version_tag": "v2-amended",
  "design_element_description": "Porous, modular layout setbacks with dedicated transit bay cutouts and trees.",
  "environmental_context": "Daytime, clean modern architecture, South Asian metropolitan context"
}
```
* **Response Payload (`ConceptRenderResponse`)**:
```json
{
  "project_id": "karachi-clifton-01",
  "version_tag": "v2-amended",
  "element_rendered": "Porous, modular layout setbacks with dedicated transit bay cutouts and trees.",
  "generated_image_url": "/static/assets/renders/concept_2738914.png",
  "revised_prompt_used": "Architectural visualization of porous modular street barriers with a dedicated transit bay cutout, leafy trees on the sidewalks, modern clean layout, daylight, Karachi metropolitan environment."
}
```

---

## 3. System Environment Registry

To set up and run the CitySmart Urban Simulation Engine locally or in a staging environment, the following configuration parameters must be supplied:

| Variable Name | Required | Default Value | Description / Staging Context |
| :--- | :---: | :--- | :--- |
| `DATABASE_URL` | Yes | `postgresql+asyncpg://postgres:postgres@localhost:5432/citysmart` | Asynchronous SQLAlchemy connection string targeting Cloud SQL PostgreSQL with `pgvector` enabled. |
| `GOOGLE_APPLICATION_CREDENTIALS` | Yes | `temp_sa_key.json` | Relative or absolute path to the GCP Service Account IAM Key JSON file. |
| `GCP_PROJECT_ID` | No | `sinuous-branch-411610` | The target identifier of the Google Cloud Project hosting Vertex AI LLM APIs and Pub/Sub streams. |
| `SUBSCRIPTION_NAME` | No | `simulation-trigger-sub` | The target GCP Pub/Sub subscription name mapped to incoming simulation triggers. |
| `PORT` | No | `8000` | Local or staging Uvicorn server runtime connection port. |

---
*CitySmart System Documentation Matrix • Generated May 2026*
