# Concurrent Kafka Consumer

This project demonstrates a multi-threaded Kafka consumer approach using Spring Boot, with a dedicated implementation that processes messages concurrently across different threads.

### Sequence Diagram



### Format code

```shell
./mvnw spotless:apply
```

### Run tests

```shell
./mvnw clean verify
```

### Run locally

```shell
$ docker-compose -f docker/docker-compose.yml up -d
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Using Testcontainers at Development Time
You can run `TestConcurrentKafkaConsumer.java` from your IDE directly.
You can also run the application using Maven as follows:

```shell
./mvnw spring-boot:test-run
```


### Useful Links
* Actuator Endpoint: http://localhost:8080/actuator
