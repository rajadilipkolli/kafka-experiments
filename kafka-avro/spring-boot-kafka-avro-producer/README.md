# kafka avro producer



### Run locally
`$ ./mvnw spring-boot:run`

### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
* Schema Registry : http://localhost:8081/subjects/persons-value/versions?normalize=false


#Reference
   - https://github.com/eddumelendez/spring-boot-kafka-testcontainers-reusable-mode/tree/main

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| HTTP Request -> Producer | Bounded future wait | `app.producer.send-timeout-ms=12000`, `max.block.ms=5000`, `delivery.timeout.ms=10000` | Timed wait throws `ResponseStatusException` mapped to HTTP 503 |
| Producer -> Kafka Broker | Bounded Kafka client memory buffer | `buffer.memory=33554432`, `max.block.ms=5000` | Blocks thread up to 5s, throws 503 on buffer saturation |

#### Known Limitations
- HTTP request latency now directly includes the broker acknowledgment latency.
- Global request-rate limiting belongs to the HTTP reverse proxy / API gateway layer.