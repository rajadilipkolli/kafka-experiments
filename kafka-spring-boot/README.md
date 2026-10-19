# Kafka Samples using spring boot

This folder contains sample kafka producer and consumer using java, spring & Spring Boot


| Type                                                                  | Description                                                                                                  |
|-----------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| [Spring Boot Kafka Sample](./boot-kafka-sample)                       | Producing and consuming Messages using Spring Boot, start and stop listener on demand using exposed endpoint |
| [Multiple Producers & Consumers](./boot-multiple-producers-consumers) | Producing and consuming Messages from Multiple Producer and consumers using Spring Boot                      |
| [Concurrent Consumer](./boot-concurrent-kafka-consumer)               | Consuming Messages Concurrently using new Thread per consumer with Spring Boot                               |

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| HTTP Request -> Producer | Bounded future wait and buffer configuration | `app.producer.send-timeout-ms=12000`, `buffer.memory=33554432`, `max.block.ms=5000` | Producer waits for broker acknowledgment; maps buffer exhaustion or timeout to 503 ProblemDetail |
| Kafka Broker -> Consumer | Bounded poll records & bounded in-memory state | `max.poll.records=50`, `max.poll.interval.ms=300000`, bounded history/maps | Consumers limit poll batches; in-memory observation structures evict oldest entries on capacity overflow |

#### Known Limitations
- HTTP request latency now directly includes broker acknowledgment roundtrip.
- Global request-rate limiting belongs to the HTTP reverse proxy / gateway layer.
