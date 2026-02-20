from fastapi import APIRouter
import app.kafka_consumer as kafka_consumer

# ─── Router (routes only) ─────────────────────────────────────────────
router = APIRouter()

@router.get("/")
def root():
    return {"message": "FastAPI + Kafka running ✅"}

@router.get("/last")
def get_last_message():
    if kafka_consumer.last_message is None:
        return {"last_message": "No messages received yet"}
    return {"last_message": kafka_consumer.last_message}