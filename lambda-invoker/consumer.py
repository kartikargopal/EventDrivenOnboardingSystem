import os
import time
import json
import requests
from kafka import KafkaConsumer

KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP", "kafka:29092")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC", "user-created-topic")
LAMBDA_INVOKE_URL = os.getenv("LAMBDA_INVOKE_URL", "http://notification-service:8080/2015-03-31/functions/function/invocations")
GROUP_ID = os.getenv("KAFKA_GROUP", "lambda-invoker-group")

def start():
    print(f"Lambda-invoker starting. Kafka={KAFKA_BOOTSTRAP} topic={KAFKA_TOPIC} -> Lambda={LAMBDA_INVOKE_URL}")
    # consumer expects JSON string values
    consumer = KafkaConsumer(
        KAFKA_TOPIC,
        bootstrap_servers=[KAFKA_BOOTSTRAP],
        auto_offset_reset='earliest',
        enable_auto_commit=True,
        group_id=GROUP_ID,
        value_deserializer=lambda m: m.decode('utf-8')
    )
    for msg in consumer:
        try:
            print("Kafka message received:", msg.value)
            # send message body as the event payload to Lambda runtime
            headers = {"Content-Type": "application/json"}
            # The Lambda runtime API expects the raw event in body
            resp = requests.post(LAMBDA_INVOKE_URL, headers=headers, data=msg.value, timeout=30)
            print("Lambda invocation response:", resp.status_code, resp.text)
        except Exception as e:
            print("Invoker error:", str(e))
            time.sleep(2)

if __name__ == "__main__":
    start()
