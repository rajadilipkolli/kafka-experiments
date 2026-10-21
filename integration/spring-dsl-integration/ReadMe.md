# spring-dsl-integration

### Format code

```shell
$ ./mvnw spotless:apply
```

### Run tests

```shell
$ ./mvnw clean verify
```

### Run locally

```shell
$ docker-compose -f docker/docker-compose.yml up -d
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Using Testcontainers at Development Time
You can run `TestApplication.java` from your IDE directly.
You can also run the application using Maven as follows:

```shell
./mvnw spring-boot:test-run
```


### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| Kafka Broker -> Inbound Adapter | Bounded poll batching | `max.poll.records=10`, `max.poll.interval.ms=300000` | Bounded records per poll; keeps poll loop active |
| Inbound Adapter -> `fromKafka` Channel | Bounded `QueueChannel` with adapter `sendTimeout` | `queueCapacity=50`, `sendTimeout=5000ms` | Blocks adapter thread up to 5s on channel full; on timeout throws `MessageDeliveryException` retried by `DefaultErrorHandler` |
| Error Handler -> Retry Backoff | `DefaultErrorHandler` with `FixedBackOff` | Backoff 100ms, unlimited attempts | Retries delivering the un-enqueued message without dropping it |

#### Known Limitations
- **Ack-on-enqueue limitation**: The listener container operates in `AckMode.RECORD` and acknowledges messages as soon as they are successfully enqueued to `fromKafka`. If an unhandled failure occurs downstream after the message is in the queue, it cannot be re-fetched from Kafka.
- HTTP latency now includes the broker acknowledgment on synchronous producers.
- Global request-rate limits belong to the HTTP layer or infrastructure.
