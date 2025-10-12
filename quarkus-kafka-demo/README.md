# Quarkus Kafka Demo

This module demonstrates a minimal Quarkus application with a Kafka producer (exposed via a REST endpoint) and a Kafka consumer (SmallRye Reactive Messaging).

How it works
- POST to /produce with a raw string body to send a message to Kafka topic `demo-topic`.
- The consumer listens on the same topic and logs received messages to stdout.

Build
1. Ensure you have Java 25 and Maven installed.
2. Start a Kafka broker (localhost:9092). You can use the repository's docker-compose files or another Kafka installation.
3. From the repository root run:

```
mvn -f quarkus-kafka-demo/pom.xml -DskipTests package
```

Run

```
mvn -f quarkus-kafka-demo/pom.xml quarkus:dev
```

Or run the packaged JAR:

```
java -jar quarkus-kafka-demo/target/quarkus-kafka-demo-1.0.0-SNAPSHOT-runner.jar
```

Test

```
curl -X POST http://localhost:8080/produce -d "hello from quarkus"
```

You should see the consumer print the message to the application logs.
