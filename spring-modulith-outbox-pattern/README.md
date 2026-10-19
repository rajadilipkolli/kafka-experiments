# spring-modulith-outbox-pattern

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

### Notes
The event published is sent to Internal Application Listener(Publish) and Extenal Application(Kafka)

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| HTTP Request -> Order API | Request validation & page size clamping | `application.max-item-count=100`, `application.max-page-size=50` | Capped page size (`Math.min(requested, 50)`); requests with >100 items rejected with 400 Bad Request |
| Transaction -> Outbox Log | Durable relational table storage | PostgreSQL `event_publication` table | Outbox acts as persistent durable backlog buffer; decouples HTTP transactions from Kafka broker availability |
| Application Restart -> Event Replay | Bounded startup resubmission | `republish-outstanding-events-on-restart=false`, `resubmission-batch-size=100`, `resubmission-max-in-flight=50`, `resubmission-min-age=5m`, `task concurrency-limit=10` | Disables unbounded mass replay on boot; resubmits stalled incomplete events with bounded batches and bounded concurrency |

#### Known Limitations
- **Durable backlog & archive growth**: Completed events are retained in `event_publication` with completion mode `archive`. Without a purge process, database table size grows indefinitely over time.
- **Collection-based stats and health scans**: Administrative endpoints (`findCompletedEvents()`) perform unpaginated list scans of the table, which degrades under heavy event history.
- Global request-rate limiting belongs to the HTTP reverse proxy / gateway layer.