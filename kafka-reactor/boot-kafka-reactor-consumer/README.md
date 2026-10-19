# boot-kafka-reactor-consumer

### Run tests
`$ ./mvnw clean verify`

### Run locally
```shell
$ docker-compose -f docker/docker-compose.yml up -d
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
### Using Testcontainers at Development Time
```shell
./mvnw spotless:apply spring-boot:test-run
```

### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
* Kafdrop UI: http://localhost:9000
* PGAdmin UI: http://localhost:5050 (pgadmin4@pgadmin.org/admin)

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| Kafka Broker -> Reactor Listener | Reactive pull-based demand with bounded poll batching | `spring.kafka.consumer.max-poll-records=50`, `max.poll.interval.ms=300000`, `concurrency=3` | Fetches bounded batch of 50 records; suspends new polls until pipeline consumes batch |
| Listener -> R2DBC Database | Reactive pipeline with async ack | Non-blocking reactive repository saves | Downstream backpressure propagated upstream through reactive stream demand |

#### Known Limitations
- **MANUAL async acknowledgment override**: Returning a `Mono` from the listener handler causes Spring Kafka to switch to MANUAL async acknowledgment upon `Mono` completion. As a result, the configured `spring.kafka.listener.ack-mode=RECORD` property is not effective.
- Global request-rate limits belong to infrastructure or API gateway layers.

