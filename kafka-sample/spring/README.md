# Connecting to kafka using only Spring

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| Spring Producer -> Kafka | Explicit buffer settings & fixed sender pacing | Buffer memory, periodic delay | Throttles message generation using fixed sleep intervals to match broker ingestion |
| Kafka Broker -> Spring Listener | Spring Message Listener Container | Standard poll batching | Pulls bounded record batches; commits offset after listener method returns |

#### Known Limitations
- Fixed sender sleep pacing does not adapt dynamically to broker backpressure or throughput fluctuations.
- Global request-rate limiting belongs to infrastructure layers.
