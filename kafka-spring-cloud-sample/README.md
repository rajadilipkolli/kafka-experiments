# Kafka Spring Cloud Sample

### Run tests
```shell
./mvnw clean verify
```

### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* kafdrop: http://localhost:9000



### To enable batch mode we need to set below property values

```properties
spring.cloud.stream.bindings.receive-in-0.consumer.batch-mode=true

# Forces consumer to wait up to 5 seconds before polling for messages, batch size bounded by below properties
spring.cloud.stream.kafka.bindings.receive-in-0.consumer.configuration.fetch.max.wait.ms=5000
spring.cloud.stream.kafka.bindings.receive-in-0.consumer.configuration.fetch.min.bytes=1048576
spring.cloud.stream.kafka.bindings.receive-in-0.consumer.configuration.max.poll.records=500
spring.cloud.stream.kafka.bindings.receive-in-0.consumer.configuration.max.poll.interval.ms=300000
```

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| HTTP Controller -> `StreamBridge` | Synchronous binder producer with boolean result check | `producer.sync=true`, `app.producer.send-timeout-ms=12000`, `max.block.ms=5000` | `StreamBridge.send` operates synchronously; returns `false` or throws on overload/timeout, mapped to 503 Service Unavailable |
| Kafka Broker -> Cloud Stream Consumer | Bounded fetch and poll batching | `max.poll.records=500`, `fetch.min.bytes=1048576`, `fetch.max.wait.ms=5000`, `max.poll.interval.ms=300000` | Bounded records per poll; bounds batch memory consumption and protects consumer heartbeat |
| Consumer -> Dead Letter Queue | `DefaultErrorHandler` with bounded backoff | `FixedBackOff(1000L, 3L)` | Retries failed record up to 3 times before routing to DLQ |

#### Known Limitations
- HTTP request latency now directly includes the broker acknowledgment latency due to synchronous producer mode.
- Global request-rate limiting belongs to infrastructure / gateway layer.
