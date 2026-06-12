import asyncio
import os
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy import text
from dotenv import load_dotenv

# Load variables from your .env file
load_dotenv()
DATABASE_URL = os.getenv("DATABASE_URL")

async def test_connection():
    print("Connecting to your new Cloud PostgreSQL backend...")
    if not DATABASE_URL or "+asyncpg" not in DATABASE_URL:
        print("❌ CONFIG ERROR: DATABASE_URL is missing or doesn't include '+asyncpg'")
        return

    try:
        # Initialize the async engine
        engine = create_async_engine(DATABASE_URL)
        
        # Execute a fast live query to check health
        async with engine.connect() as conn:
            result = await conn.execute(text("SELECT version();"))
            db_version = result.scalar()
            print("\n✅ SUCCESS: Handshake complete! Your cloud database is working perfectly.")
            print(f"Cloud DB Engine Version: {db_version}")
            
        await engine.dispose()
    except Exception as e:
        print("\n❌ CONNECTION FAILED: Backend rejected the request.")
        print(f"Error details: {e}")

if __name__ == "__main__":
    asyncio.run(test_connection())