# Analytics Spring Cloud Streams Kafka Producer

Spring Cloud Stream producer supplying periodic synthetic page view events to Kafka.

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| Supplier Function -> Kafka Topic | Supplier poller fixed delay | `producer.poller.fixed-delay=1000ms`, `poller.fixed-delay=1000ms` | Emits events at bounded rate (1/sec), avoiding topic and socket saturation |
| Producer -> Kafka Broker | Bounded Kafka client settings | Standard buffer memory and idempotence | Backs off if Kafka cluster cannot accept outgoing messages |

#### Known Limitations
- Fixed delay pacing does not dynamically scale back based on downstream consumer lag.
- Global request-rate limiting belongs to infrastructure layers.
