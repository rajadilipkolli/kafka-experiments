# Quarkus Kafka Demo

This module demonstrates a minimal Quarkus application with a Kafka producer (exposed via a REST endpoint) and a Kafka consumer (SmallRye Reactive Messaging).

How it works
- POST to /produce with a raw string body to send a message to Kafka topic `demo-topic`.
- The consumer listens on the same topic and logs received messages to stdout.

Build
1. Ensure you have Java 25 and Maven installed.
2. Start a Kafka broker (localhost:9092). You can use the repository's docker-compose files or another Kafka installation.
3. From the repository root run:

```shell
./mvnw -f quarkus-kafka-demo/pom.xml -DskipTests package
```

Run

```shell
./mvnw -f quarkus-kafka-demo/pom.xml quarkus:dev
```

Or run the packaged JAR:

```shell
java -jar quarkus-kafka-demo/target/quarkus-kafka-demo-1.0.0-SNAPSHOT-runner.jar
```

Test

```shell
curl -X POST http://localhost:8080/produce -d "hello from quarkus"
```

You should see the consumer print the message to the application logs.

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| HTTP Request -> `ProducerResource` | Async emitter stage with bounded overflow buffer | `@OnOverflow(BUFFER, bufferSize=1000)`, `max-inflight-messages=1024`, `waitForWriteCompletion=true`, `app.producer.send-timeout=10s` | Returns 503 when the HTTP completion deadline expires; returns 202 on write completion; returns 503 on nack, async failure, or synchronous buffer overflow; returns 500 on JSON serialization failure |
| Kafka Broker -> SmallRye Incoming Channel | Reactive pull with bounded batch and queue factor | `max.poll.records=50`, default `max-queue-size-factor=2`, default `pause-if-no-requests=true` | Pauses Kafka polling when internal queue reaches `max-queue-size-factor * max.poll.records` or if downstream has no active demand (`pause-if-no-requests`) |
| Consumer Bean -> `RECEIVED` Deque | Bounded FIFO memory buffer with drop-oldest eviction | Capacity = 1000 | Evicts oldest message on buffer full before re-offering, preventing memory growth |

#### Known Limitations
- **HTTP latency includes broker acknowledgment**: With `waitForWriteCompletion=true` (the default in SmallRye Kafka), the HTTP response waits for acknowledgment up to `app.producer.send-timeout` (default 10 seconds). A timeout returns 503; the send can still succeed later without changing the HTTP response, so retries may duplicate messages.
- **SmallRye pausing behavior**: When no requests are emitted or the internal buffer fills (`max-queue-size-factor`), the consumer pauses Kafka partition polling.
- Global request-rate limiting belongs to infrastructure layers.
