# CitySmart Architecture & Agent Manifest

## System Philosophy
Headless, decoupled, API-first Multi-Agent System (MAS) built to simulate urban infrastructure dynamics.

## Tech Stack Contract
- **Backend Framework:** FastAPI (Asynchronous Python 3.12+)
- **Database Tier:** Cloud SQL for PostgreSQL 16 with `pgvector` extension
- **ORM Layer:** SQLAlchemy 2.0 (Async Engine) + `asyncpg`
- **Message Bus:** Google Cloud Pub/Sub (Decoupled execution queue)
- **Real-Time Delivery:** Cloud Firestore Asynchronous Shared-State Store
