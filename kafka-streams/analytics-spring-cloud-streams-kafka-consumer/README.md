# Analytics Spring Cloud Streams Kafka Consumer

Kafka Streams application consuming page view events and aggregating counts using a materialized key-value store with interactive query capabilities.

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| Kafka Broker -> Streams Consumer | Controlled stream thread concurrency and RocksDB cache | `num.stream.threads=2`, `statestore.cache.max.bytes=10485760` (10MB), `commit.interval.ms=1000` | Paces consumption according to stream processor capacity and flushes cached aggregates periodically |
| Interactive Query -> State Store | Deterministic iterator disposal | Try-with-resources on `ReadOnlyKeyValueStore.all()` | Closes `KeyValueIterator` promptly to prevent RocksDB native memory leaks |

#### Known Limitations
- **Cumulative state growth**: The state store retains counts across all observed keys indefinitely without windowing or TTL. Storage and memory growth are proportional to unique key cardinality.
- Global request-rate limiting on the interactive query REST endpoints belongs to infrastructure/gateway layers.
