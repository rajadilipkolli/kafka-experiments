# Connecting to kafka using plain Java

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| Kafka Broker -> Consumer | Single poll loop | Poll timeout = 100ms | Pulls records only when previous batch has completed processing |
| Producer -> Kafka Broker | Synchronous `Future.get()` | Default producer buffer memory | Blocks sending thread until acknowledgment is received |

#### Known Limitations
- **Cumulative count cardinality**: The consumer tracks cumulative processed records in memory without truncation or windowing; distinct key cardinality grows over time.
- Global request-rate limits belong to upstream infrastructure.
