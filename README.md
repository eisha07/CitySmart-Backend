# CitySmart: Urban Infrastructure Multi-Agent Social Digital Twin

CitySmart is an API-first, asynchronous backend platform designed to simulate and manage urban infrastructure dynamics. It leverages a Multi-Agent System (MAS) to transform unstructured urban planning proposals into structured, actionable data insights, complete with semantic search capabilities and real-time event-driven processing.

## 🏛 Architecture Overview

The system follows a headless, decoupled architecture built for scalability and environment-aware execution.

*   **API Gateway:** FastAPI (Python 3.12+) providing high-performance asynchronous endpoints.
*   **Intelligence Layer:** Google Gemini 2.5 Flash for automated schema extraction and agent reasoning.
*   **Vector Engine:** PostgreSQL 16 with `pgvector` for storing high-dimensional embeddings and relational metadata.
*   **Message Bus:** Google Cloud Pub/Sub for triggering asynchronous simulation loops.
*   **Storage Tier:** Google Cloud Storage (GCS) for archival logging, with a local filesystem fallback.
*   **Embeddings:** Vertex AI `text-embedding-004` for semantic representation.

## 🤖 Agents & Core Intelligence

The core of CitySmart is a set of specialized agents that collaborate to process urban data:

1.  **Extraction Agent:** Uses Gemini 2.5 Flash to parse raw, unstructured user prompts into a strict JSON schema (`project_id`, `title`, `proposal_text`).
2.  **Semantic Broker:** Orchestrates the chunking of document text and interfaces with Vertex AI to generate embeddings for RAG (Retrieval-Augmented Generation).
3.  **Simulation Engine:** A background worker (Pub/Sub Subscriber) that listens for new project triggers and initiates deeper agent-based analysis across the "Digital Twin" infrastructure.

## ⚙️ Mock vs. Real API Integration

The system is designed with a strict "Local-Production" separation to allow for development without incurring cloud costs:

| Service | Environment: **Local** | Environment: **Production** |
| :--- | :--- | :--- |
| **Database** | Local PostgreSQL Container / Host | Cloud SQL for PostgreSQL |
| **Pub/Sub** | Pub/Sub Emulator (`localhost:8085`) | Real GCP Pub/Sub Topics/Subs |
| **Storage** | Local FS (`app/static/assets/logs`) | Google Cloud Storage Buckets |
| **Embeddings** | Zero-Vector Mock (768-dim) | Vertex AI Text Embedding Model |
| **LLM (Gemini)** | Live API (via API Key) | Vertex AI / GenAI SDK |

## 🚀 Integration Flow: The Ingestion Pipeline

1.  **POST `/api/v1/projects/ingest`**: Receives raw text.
2.  **Schema Extraction**: Gemini filters and structures the unstructured input.
3.  **Archival**: The raw text is uploaded to GCS (or local mock).
4.  **Database Persistence**: Core project data is saved to PostgreSQL.
5.  **Vectorization**: Text is chunked, embedded via Vertex AI, and saved to `pgvector`.
6.  **Event Dispatch**: A trigger is published to Pub/Sub to signal background agents.

## 🛠 Setup & Execution

### Prerequisites
- Docker & Docker Compose
- Google Cloud Service Account Key (`temp_sa_key.json`)
- Gemini API Key

### Running with Docker
```bash
docker build -t citysmart-backend .
docker run -d -p 8000:8000 \
  -e GEMINI_API_KEY="your_key" \
  -e ENVIRONMENT="production" \
  -e DATABASE_URL="postgresql+asyncpg://user:pass@host:port/db" \
  -v "$(pwd)/temp_sa_key.json:/app/temp_sa_key.json" \
  citysmart-backend
```

## 📜 Design Philosophy
CitySmart is built on the principle of **Failure Isolation**. Every service call (GCS, Vertex, Pub/Sub) is wrapped in diagnostic handlers that allow the system to proceed with partial data or local fallbacks even if a specific cloud service undergoes a transient failure.
