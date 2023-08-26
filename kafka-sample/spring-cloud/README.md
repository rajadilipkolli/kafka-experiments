

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

# Forces consumer to wait 5 seconds before polling for messages, batch size depends on below properties
spring.cloud.stream.kafka.bindings.receive-in-0.consumer.configuration.fetch.max.wait.ms=5000
spring.cloud.stream.kafka.bindings.receive-in-0.consumer.configuration.fetch.min.bytes=1000000000
spring.cloud.stream.kafka.bindings.receive-in-0.consumer.configuration.max.poll.records=10000000
```
