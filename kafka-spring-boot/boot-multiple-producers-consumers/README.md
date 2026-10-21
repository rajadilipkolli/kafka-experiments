

### Run tests
`$ ./mvnw clean verify`

### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* kafdrop: http://localhost:9000
* Zipkin: http://localhost:9411

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| HTTP Controller -> `Sender` | Timed future wait with 503 mapping | `app.producer.send-timeout-ms=12000`, `buffer.memory=33554432`, `max.block.ms=5000` | Replaced `.get()` with timed wait; propagates throwable, restores interrupt flag, maps send failure or timeout to HTTP 503 |
| Producer -> Kafka Broker | Bounded buffer memory and block timeout | `buffer.memory=33554432`, `max.block.ms=5000` | Blocks producer thread up to 5s before shedding load with 503 |

#### Known Limitations
- HTTP request latency now directly includes the broker acknowledgment latency.
- Global request-rate limiting belongs to the HTTP reverse proxy / gateway layer.
