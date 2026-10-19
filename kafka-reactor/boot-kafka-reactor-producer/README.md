# boot-kafka-reactor-producer

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
| HTTP WebFlux Request -> `MessageService` | Reactive `Mono` wait with timeout | `app.producer.send-timeout-ms=12000`, `timeout(12s)` | On timeout, emits `TimeoutException` mapped to HTTP 503 ProblemDetail |
| `KafkaSender` -> Broker | Bounded buffer and max in-flight | `buffer.memory=33554432`, `max.block.ms=5000`, `maxInFlight=1024` | Blocks producer thread or emits failure on broker buffer saturation mapped to HTTP 503 |

#### Known Limitations
- HTTP request latency now directly includes the broker acknowledgment roundtrip.
- Global request-rate limiting belongs to the HTTP reverse proxy / gateway layer.

