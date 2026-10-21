# Kafka Samples

This folder contains sample kafka producer and consumer using java, spring & Spring Boot


| Type                                                                         | Description                                                                             |
|------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| [Java](./java)                                                               | Producing and consuming Messages using Plain Java and Kafka                             |
| [Spring](./spring)                                                           | Producing and consuming Messages using Pure Spring                                      |

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| Java Producer -> Broker | Synchronous metadata `.get()` on produce | Default client buffer memory | Blocks caller thread until send acknowledgment or timeout |
| Java Consumer -> Loop | Single pull-loop poll | Standard poll timeout (100ms) | Natural pull pacing; consumes only when previous batch finishes |
| Spring Producer -> Broker | Explicit buffer settings & fixed sender pacing | Buffer memory, pacing intervals | Throttles send rate to prevent socket/buffer flooding |

#### Known Limitations
- In `kafka-sample/java`, consumer maintains cumulative message count cardinality in memory.
- In `kafka-sample/spring`, sender relies on fixed thread sleep pacing rather than reactive feedback.
- Global request-rate limiting belongs to infrastructure layers.
