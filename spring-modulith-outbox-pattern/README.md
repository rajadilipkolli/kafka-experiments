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
Namastack Outbox handles event externalization to external applications such as Kafka. Internal application listeners receive events through the normal application event path.

### Backpressure

| Boundary                          | Mechanism                               | Configured Limits                                                                           | Behavior on Overload                                                                                                                                                                     |
|:----------------------------------|:----------------------------------------|:--------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| HTTP Request -> Order API         | Request validation & page size clamping | `application.max-item-count=100`, `application.max-page-size=50`                            | Capped page size (`Math.min(requested, 50)`); requests with >100 items rejected with 400 Bad Request                                                                                     |
| Transaction -> Outbox Log         | Distributed outbox table storage        | PostgreSQL `outbox_record` table via Namastack Outbox (`spring-modulith-starter-namastack`) | Outbox acts as persistent durable backlog buffer; decouples HTTP transactions from Kafka broker availability                                                                             |
| Outbox Dispatcher -> Kafka Broker | Partition-based polling & retry         | `spring.modulith.events.externalization.mode=outbox`                                        | Partition-based distributed coordination (256 partitions) with heartbeat lease timeouts. Automatic failover and retry on downstream broker issues without manual reconciliation scripts. |

#### Known Limitations
- **Outbox table retention**: Completed events remain in `outbox_record` with status `COMPLETED`. A periodic purge or TTL should clean up historical completed entries in production.
- Global request-rate limiting belongs to the HTTP reverse proxy / gateway layer.
