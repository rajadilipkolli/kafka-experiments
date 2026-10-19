# Camel Boot Integration

Demonstrates Apache Camel routes integrated with Kafka and Spring Boot.

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| Timer Producer -> Kafka | Periodic timer pacing | Fixed period = 1000ms | Paces production rate at 1 msg/sec; pauses generation between ticks |
| Camel Route -> Consumer | Synchronous route invocation | Single-threaded route execution | Pauses next poll/step until current exchange completes |

#### Known Limitations
- Fixed timer pacing cannot dynamically react to broker slowdowns or network partitions.
- Global request-rate limiting belongs to infrastructure or API gateway layers.
- HTTP latency includes broker acknowledgment when producers operate synchronously.
