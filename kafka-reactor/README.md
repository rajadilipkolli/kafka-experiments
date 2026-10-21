# Kafka Reactor

Kafka Reactor is a non-blocking driver

This repository contains sample projects integrating with kafka reactor

  - [consumer](./boot-kafka-reactor-consumer/README.md) - Consuming events using reactive mechanism and saves in database.
  - [producer](./boot-kafka-reactor-producer/README.md) - Producing events using reactive mechanism

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| HTTP Request -> Reactor Producer | Reactive pipeline with timeout | `app.producer.send-timeout-ms=12000`, `maxInFlight=1024`, `buffer.memory=33554432` | Maps queue saturation or wait timeout to HTTP 503 ProblemDetail |
| Kafka Broker -> Reactor Consumer | Bounded poll records and reactive flow | `max-poll-records=50`, `max.poll.interval.ms=300000`, `concurrency=3` | Reactive stream pull demand; commits asynchronously on DB completion |

#### Known Limitations
- HTTP request latency now directly includes the broker acknowledgment latency.
- In the Reactor consumer, returning a `Mono` from the listener delegate switches to MANUAL async acknowledgment; the configured `RECORD` property is not effective.
- Global request-rate limiting belongs to the HTTP reverse proxy / gateway layer.
