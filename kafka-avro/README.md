# kafka avro


Apache Kafka is an open-source stream-processing platform that is used for building real-time data pipelines and streaming apps. It is horizontally scalable, fault-tolerant, and fast.

Apache Avro is a data serialization system that that provides a compact and efficient binary format for data serialization and is commonly used with Apache Kafka. It is a compact and efficient binary format that allows for the serialization of data with a schema. This means that the structure of the data can be defined, and the data can be self-describing, which can be useful when working with complex data structures.

Together, Kafka and Avro can be used to create a powerful platform for building real-time data pipelines and streaming applications. Avro is often used with Kafka because it supports schema evolution, which allows for the evolution of data over time without the need to update all the systems that are consuming that data. This makes it an ideal choice for use with Kafka, where data is constantly being generated and consumed by a variety of systems.

## Kafka Avro Schema Evolution Demonstration

This example demonstrates how Apache Kafka with Avro can handle schema evolution, where a producer sends messages using version 2 schema and a consumer can process both version 1 and version 2 messages seamlessly.

## Schema Evolution Overview

### Version 1 Schema (Basic Fields)
```json
{
     "namespace": "com.example.springbootkafkaavro.model",
     "type": "record",
     "name": "Person",
     "version": "1",
     "fields": [
       { "name": "id", "type": "long" },
       { "name": "name", "type": "string", "avro.java.string": "String" },
       { "name": "age", "type": "int" }
     ]
}
```

### Version 2 Schema (Extended Fields)
```json
{
     "namespace": "com.example.springbootkafkaavro.model",
     "type": "record",
     "name": "Person",
     "version": "2",
     "fields": [
       { "name": "id", "type": "long" },
       { "name": "name", "type": "string", "avro.java.string": "String" },
       { "name": "age", "type": "int" },
       { "name": "gender", "type": ["null", "string"], "default": null, "avro.java.string": "String" },
       { "name": "email", "type": ["null", "string"], "default": null, "avro.java.string": "String" },
       { "name": "phoneNumber", "type": ["null", "string"], "default": null, "avro.java.string": "String" }
     ]
}
```

## Key Features Demonstrated

1. **Backward Compatibility**: The consumer using V2 schema can read messages produced with both V1 and V2 schemas
2. **Forward Compatibility**: New fields in V2 are optional (nullable with defaults)
3. **Schema Registry Integration**: Automatic schema validation and evolution handling
4. **Consumer Flexibility**: The consumer automatically handles missing fields from older schema versions

## Producer Endpoints

### Version 1 Compatible Endpoint
```bash
POST /person/publish
```
Parameters:
- `name` (required): Person's name
- `age` (required): Person's age
- `gender` (optional): Person's gender

### Version 2 Extended Endpoint
```bash
POST /person/publish/v2
```
Parameters:
- `name` (required): Person's name
- `age` (required): Person's age
- `gender` (optional): Person's gender
- `email` (optional): Person's email address
- `phoneNumber` (optional): Person's phone number

## Consumer Behavior

The consumer (`AvroKafkaListener`) demonstrates:

1. **Schema Detection**: Automatically detects which schema version was used for each message
2. **Logging**: Detailed logging showing schema evolution in action
3. **Field Handling**: Gracefully handles missing fields (they become null)
4. **Database Storage**: Saves all available fields to the database

## Running the Demo

### Prerequisites
- Docker and Docker Compose
- Java 21
- Maven

### Start Infrastructure
```bash
cd kafka-avro/spring-boot-kafka-avro-producer/docker
docker-compose up -d
```

### Start Consumer
```bash
cd kafka-avro/spring-boot-kafka-avro-consumer
./mvnw spring-boot:run
```

### Start Producer
```bash
cd kafka-avro/spring-boot-kafka-avro-producer
./mvnw spring-boot:run
```

### Test Schema Evolution

1. **Send a V1-style message** (only basic fields):
```bash
curl -X POST "http://localhost:8080/person/publish" \
  -d "name=John Doe&age=30&gender=Male"
```

2. **Send a V2 message** (with new fields):
```bash
curl -X POST "http://localhost:8080/person/publish/v2" \
  -d "name=Jane Smith&age=25&gender=Female&email=jane@example.com&phoneNumber=555-0123"
```

3. **Check the consumer logs** to see schema evolution in action:
```console
=== SCHEMA EVOLUTION DEMO ===
Received Person with Version 1 schema (basic fields only)
Person received - Name: John Doe, Age: 30, Gender: Male
V1 message - No email/phone fields available (backward compatibility)
Person saved to database with ID: 1
=== END SCHEMA EVOLUTION DEMO ===

=== SCHEMA EVOLUTION DEMO ===
Received Person with Version 2 schema (includes email and phoneNumber fields)
Person received - Name: Jane Smith, Age: 25, Gender: Female
V2 fields detected - Email: jane@example.com, Phone: 555-0123
Person saved to database with ID: 2
=== END SCHEMA EVOLUTION DEMO ===
```

### Automated Test

Run the schema evolution test:
```bash
cd kafka-avro/spring-boot-kafka-avro-consumer
./mvnw test -Dtest=ApplicationIntTests
```

## Benefits of This Approach

1. **Zero Downtime Deployments**: Consumers can be updated to handle new schema versions without stopping the system
2. **Gradual Migration**: Producers can be updated incrementally to send new schema versions
3. **Data Integrity**: All messages are validated against their respective schemas
4. **Backward Compatibility**: Old messages continue to work with new consumers
5. **Future-Proof**: New optional fields can be added without breaking existing consumers

## Schema Evolution Rules

For successful schema evolution in Avro:

1. **Adding Fields**: New fields must have default values or be nullable
2. **Removing Fields**: Only remove fields that are no longer needed by any consumer
3. **Changing Types**: Limited type changes are allowed (e.g., int to long)
4. **Field Names**: Cannot be changed once defined

This demonstration shows how Kafka with Avro and Schema Registry provides a robust foundation for evolving data contracts in distributed systems.
