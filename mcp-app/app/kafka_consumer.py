from kafka import KafkaConsumer, KafkaProducer
import json
import requests

# ─── Config ──────────────────────────────────────────────────────────
KAFKA_BOOTSTRAP = "localhost:29092"
CONSUME_TOPIC = "sql-requests"
PRODUCE_TOPIC = "sql-responses"
OLLAMA_URL = "http://localhost:11434/api/generate"
OLLAMA_MODEL = "qwen2.5-coder:0.5b"

# ─── Kafka Consumer ───────────────────────────────────────────────────
last_message = None

def generate_sql(request: dict) -> str:
    table = request.get("table", "")
    columns = ", ".join(request.get("columns", []))
    description = request.get("description", "")

    prompt = f"""
You are an expert MySQL SQL generator.
Generate a MySQL SQL query based on:
- Table: {table}
- Columns: {columns}
- Description: {description}

Return ONLY the SQL query, nothing else.
"""
    try:
        response = requests.post(OLLAMA_URL, json={
            "model": OLLAMA_MODEL,
            "prompt": prompt,
            "stream": False
        })

        if response.status_code == 200:
            return response.json().get("response", "").strip()
        else:
            return f"Ollama error: {response.status_code}"

    except Exception as e:
        return f"Error calling Ollama: {str(e)}"


def start_kafka_consumer():
    global last_message

    # Consumer - reads from Java
    consumer = KafkaConsumer(
        CONSUME_TOPIC,
        bootstrap_servers=KAFKA_BOOTSTRAP,
        auto_offset_reset="earliest",
        group_id="fastapi-group",
        value_deserializer=lambda v: json.loads(v.decode("utf-8"))
    )

    # Producer - sends result back to Java
    producer = KafkaProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP,
        value_serializer=lambda m: json.dumps(m).encode("utf-8")
    )

    print("✅ Kafka consumer started, listening on topic: sql-requests")

    for msg in consumer:
        request = msg.value
        last_message = request
        request_id = request.get("requestId", "unknown")

        print(f"\n📩 Received request [{request_id}]: {request}")

        # Generate SQL using Ollama
        print("🤖 Sending to Ollama...")
        sql = generate_sql(request)
        print(f"✅ Generated SQL: {sql}")

        # Send result back to Java
        response_payload = {
            "requestId": request_id,
            "sql": sql,
            "status": "success"
        }
        producer.send(PRODUCE_TOPIC, value=response_payload)
        producer.flush()
        print(f"📤 Sent SQL response to topic: {PRODUCE_TOPIC}")