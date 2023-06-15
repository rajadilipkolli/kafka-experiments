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