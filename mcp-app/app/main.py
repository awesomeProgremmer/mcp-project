from contextlib import asynccontextmanager
from fastapi import FastAPI
import threading

from app.kafka_consumer import start_kafka_consumer
from app.app import router

# ─── Lifespan ─────────────────────────────────────────────────────────
@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    thread = threading.Thread(target=start_kafka_consumer, daemon=True)
    thread.start()
    print("🚀 FastAPI started, Kafka consumer running in background")
    yield
    # Shutdown
    print("🛑 FastAPI shutting down")

# ─── App ──────────────────────────────────────────────────────────────
app = FastAPI(lifespan=lifespan)
app.include_router(router)