import asyncio
import os
from dotenv import load_dotenv
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy import text
from app.core.database import Base

# Import all models so SQLAlchemy is aware of them during table generation
from app.models.database import (
    ProjectModel,
    ProjectVersionModel,
    DocumentInsightModel,
    SimulationResultModel,
)
from app.models.document_chunk import DocumentChunkModel

load_dotenv()
DATABASE_URL = os.getenv("DATABASE_URL")

async def setup_database():
    if not DATABASE_URL or "+asyncpg" not in DATABASE_URL:
        print("❌ CONFIG ERROR: DATABASE_URL is missing or doesn't include '+asyncpg' driver in .env")
        return

    print(f"Connecting to database...")
    engine = create_async_engine(DATABASE_URL)
    
    try:
        async with engine.begin() as conn:
            # 1. Enable pgvector extension
            print("Configuring pgvector extension...")
            await conn.execute(text("CREATE EXTENSION IF NOT EXISTS vector;"))
            
            # 2. Automatically generate the tables
            print("Generating tables based on backend models...")
            await conn.run_sync(Base.metadata.create_all)
            
        print("\n✅ SUCCESS: All tables have been successfully created in your database!")
    except Exception as e:
        print(f"\n❌ SETUP FAILED: {e}")
    finally:
        await engine.dispose()

if __name__ == "__main__":
    asyncio.run(setup_database())
