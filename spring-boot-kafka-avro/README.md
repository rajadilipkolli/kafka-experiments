# kafka avro


Apache Kafka is an open-source stream-processing platform that is used for building real-time data pipelines and streaming apps. It is horizontally scalable, fault-tolerant, and fast.

Apache Avro is a data serialization system that that provides a compact and efficient binary format for data serialization and is commonly used with Apache Kafka. It is a compact and efficient binary format that allows for the serialization of data with a schema. This means that the structure of the data can be defined, and the data can be self-describing, which can be useful when working with complex data structures.

Together, Kafka and Avro can be used to create a powerful platform for building real-time data pipelines and streaming applications. Avro is often used with Kafka because it supports schema evolution, which allows for the evolution of data over time without the need to update all the systems that are consuming that data. This makes it an ideal choice for use with Kafka, where data is constantly being generated and consumed by a variety of systems.


### Run locally
`$ ./mvnw spring-boot:run`

### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
* Schema Registry : http://localhost:8081/subjects/persons-value/versions?normalize=false
