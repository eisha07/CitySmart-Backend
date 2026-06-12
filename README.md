# CitySmart: Urban Infrastructure Multi-Agent Social Digital Twin

CitySmart is an API-first, asynchronous backend platform designed to simulate and manage civic urban infrastructure dynamics. It leverages a Multi-Agent System (MAS) to transform unstructured urban planning proposals into structured, actionable data insights, complete with semantic search capabilities, real-time event-driven processing, and cached database simulation states.

---

## 🏛 Tech Stack & Architecture

The system follows a headless, decoupled architecture built for high-performance, environment-aware execution:

*   **API Gateway:** FastAPI (Python 3.10+) providing high-performance asynchronous endpoints and routers.
*   **Intelligence Layer (Google GenAI):** Google Gemini 2.5 Flash as the primary model, with an automatic fallback chain consisting of `gemini-3.1-flash-lite` and `gemini-2.5-flash-lite` wrapped in an exponential backoff-with-jitter retry handler to resolve quota (`429`) and transient (`503`) demand spikes.
*   **ORM & Vector Database:** Cloud SQL / PostgreSQL 16 with `pgvector` extension for storing high-dimensional embeddings alongside relational metadata. Interfaced using SQLAlchemy 2.0 Async Engine + `asyncpg`.
*   **Message Broker:** Google Cloud Pub/Sub for triggering background simulation loops.
*   **Storage Tier:** Google Cloud Storage (GCS) for archival logging, with automatic local filesystem fallbacks.
*   **Embeddings Layer:** Vertex AI `text-embedding-004` (with a local zero-vector fallback) for semantic representations and Retrieval-Augmented Generation (RAG).
*   **Multimodal Generation:** Imagen 3 (`imagen-3.0-generate-002`) for visual mockups of proposed architectural revisions.

---

## 📂 Core Architecture Map & Service Layers

*   **[app/main.py](file:///Users/fatimatuzzahra/Downloads/CItySmart/app/main.py):** Main application gateway. Sets up lifespan handlers for Pub/Sub listeners, global middleware for logging telemetry, and custom exception mapping (SQLAlchemy to 503, Google Cloud API to 502).
*   **[app/services/agent_engine.py](file:///Users/fatimatuzzahra/Downloads/CItySmart/app/services/agent_engine.py):** Implements `UrbanAgentSimulationEngine` which controls multi-agent debate generation, consensus arbitration (Mediator Agent), Imagen prompt refinement, and state dialogue interrogation. Features the robust `_generate_with_fallback` retry method.
*   **[app/services/pubsub_worker.py](file:///Users/fatimatuzzahra/Downloads/CItySmart/app/services/pubsub_worker.py):** Implements `CloudPubSubWorkerLoop` for non-blocking background subscription streams that execute simulation pipelines and persist structured synthesis payloads to the database.
*   **[app/services/vertex_ai.py](file:///Users/fatimatuzzahra/Downloads/CItySmart/app/services/vertex_ai.py):** Manages `VertexEmbeddingService` which chunks input texts and generates 768-dimensional text embeddings.
*   **[app/services/gcp_resources.py](file:///Users/fatimatuzzahra/Downloads/CItySmart/app/services/gcp_resources.py):** Configures GCS text upload service with local path backups (`app/static/assets/logs`) and Secret Manager integration.
*   **[app/models/database.py](file:///Users/fatimatuzzahra/Downloads/CItySmart/app/models/database.py):** Configures the SQLAlchemy entity schemas:
    *   `ProjectModel` (relational profile meta)
    *   `ProjectVersionModel` (branches design amendments)
    *   `DocumentInsightModel` (stores semantic chunks and pgvector embeddings)
    *   `SimulationResultModel` (simulation results table containing payload results)

---

## ⚡ Core Features & API Endpoints

### 1. Ingestion Pipeline
*   **`POST /api/v1/projects/ingest`**: Receives raw unstructured text. Automatically extracts schema properties using Gemini (`project_id`, `title`, `baseline_proposal_text`), archives raw contents to GCS, chunks and embeds insights into `pgvector` for RAG context, and dispatches a Pub/Sub event to background agents.

### 2. Multi-Agent Digital Twin Simulation
*   **`GET /api/v1/simulation/{project_id}/state`**: Evaluates the latest project design by simulating the viewpoints of 10 localized citizen personas (e.g. rickshaw drivers, storefront shopkeepers, students). 
*   **Query Caching Benefit**: Simulation results are stored in the `simulation_results` table. Subsequent calls to `/state` retrieve the cached payload instantly, bypassing Gemini API invocation. Users can bypass the cache and force regeneration using the query parameter `?force_refresh=true`.
*   **`POST /api/v1/simulation/chat`**: Enables interactive stateful chat dialogue directly with any citizen persona.
*   **`POST /api/v1/simulation/render-concept`**: Uses Imagen 3 to generate a visual mockup from architectural descriptions.

### 3. Analytics Dashboard
*   **`GET /api/v1/dashboard/{project_id}`**: Renders a dark-mode administrative analytical dashboard displaying social, economic, and political scores, arbitrator verdicts, and prescription revisions.

---

## ⚙️ Service Environment Partitioning

| Service / Resource | Local Mode (`ENVIRONMENT="local"`) | Production Mode (`ENVIRONMENT="production"`) |
| :--- | :--- | :--- |
| **Database** | Local PostgreSQL Container / Host | Cloud SQL for PostgreSQL |
| **Pub/Sub** | Emulator (`localhost:8085`) | Real GCP Pub/Sub Topic/Subscription |
| **Storage** | Local Filesystem (`app/static/assets/logs`) | Google Cloud Storage Bucket |
| **Embeddings** | Zero-Vector Fallback (768-dim) | Vertex AI `text-embedding-004` |
| **LLM (Gemini)** | Developer API Key (`GEMINI_API_KEY`) | Vertex AI / SDK Credentials |

---

## 🛠 Setup & Local Execution

### 1. Configure the Environment
Create a `.env` file in the project root:
```env
ENVIRONMENT="local"
DATABASE_URL="postgresql+asyncpg://postgres:postgres@localhost:5432/citysmart"
GEMINI_API_KEY="AIzaSy..."
GCP_PROJECT_ID="your-project-id"
GCP_REGION="asia-northeast3"
```

### 2. Initialize Database
Initialize the database tables and enable the `pgvector` extension:
```bash
python setup_db.py
```

### 3. Run the Backend API Server
Start the server locally with auto-reload enabled:
```bash
PYTHONPATH=. python -m app.main
```
The documentation will be available at `http://localhost:8000/docs` (Swagger UI).

### 4. Running the Tests
Run the test suite using `pytest`:
```bash
python -m pytest tests/
```

### 5. Running with Docker
```bash
docker build -t citysmart-backend .
docker run -d -p 8000:8000 \
  -e GEMINI_API_KEY="your_key" \
  -e ENVIRONMENT="production" \
  -e DATABASE_URL="postgresql+asyncpg://user:pass@host:port/db" \
  -v "$(pwd)/temp_sa_key.json:/app/temp_sa_key.json" \
  citysmart-backend
```
