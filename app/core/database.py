import os
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlalchemy.orm import DeclarativeBase
from app.core.config import settings

# Fallback to a safe async template string if DATABASE_URL isn't explicitly cached yet in local .env
# Secret Manager tokens will overwrite this at runtime in production envs
DATABASE_URL = settings.DATABASE_URL or os.getenv(
    "DATABASE_URL",
    f"postgresql+asyncpg://postgres:placeholder_password@127.0.0.1:5432/{settings.GCP_PROJECT_ID}",
)

# Create the high-performance async engine tailored for transactional throughput
engine = create_async_engine(
    DATABASE_URL,
    echo=False,  # Set to True during deep debugging sessions
    pool_size=20,  # Maintain a healthy connection pool size for multi-agent loops
    max_overflow=10,
    pool_pre_ping=True,  # Automatically check connection liveliness before executing queries
    pool_recycle=1800,  # Recycle connections after 30 minutes to prevent timeouts
    pool_timeout=30,  # Prevent infinite hangs when acquiring connections from pool
)

# Construct our scoped session maker for route-level dependency injection
AsyncSessionLocal = async_sessionmaker(
    bind=engine, class_=AsyncSession, expire_on_commit=False
)


# Establish the unified Declarative Base for our structural database tables/models
class Base(DeclarativeBase):
    pass


# Dependency injection helper to yield an active database session per endpoint request
async def get_db():
    async with AsyncSessionLocal() as session:
        try:
            yield session
        finally:
            await session.close()
