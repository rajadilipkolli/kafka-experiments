package com.example.springbootkafkaavro.common;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class KafkaContainersConfig {

    private final Network network = Network.newNetwork();
    private final String CONFLUENT_VERSION = "8.0.0";

    @Bean
    @ServiceConnection
    ConfluentKafkaContainer kafkaContainer() {
        return new ConfluentKafkaContainer(
                        DockerImageName.parse("confluentinc/cp-kafka").withTag(CONFLUENT_VERSION))
                .withListener("tc-kafka:19092") // Internal alias and port
                .withNetwork(network) // Shared network for communication
                .withNetworkAliases("tc-kafka") // Alias to match Schema Registry
                .withReuse(true);
    }

    @Bean
    @DependsOn("kafkaContainer")
    GenericContainer<?> schemaRegistry() {
        return new GenericContainer<>(
                        DockerImageName.parse("confluentinc/cp-schema-registry")
                                .withTag(CONFLUENT_VERSION))
                .withExposedPorts(8085)
                .withNetworkAliases("schemaregistry") // Alias for Schema Registry
                .withNetwork(network) // Use the same network as Kafka
                .withEnv(
                        "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
                        "PLAINTEXT://tc-kafka:19092") // Match Kafka alias and port
                .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8085")
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schemaregistry")
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL", "PLAINTEXT")
                .waitingFor(Wait.forHttp("/subjects").forStatusCode(200))
                .withStartupTimeout(Duration.ofSeconds(60));
    }

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(GenericContainer<?> schemaRegistry) {
        return dynamicProperty -> {
            dynamicProperty.add(
                    "spring.kafka.producer.properties.schema.registry.url",
                    () ->
                            "http://%s:%d"
                                    .formatted(
                                            schemaRegistry.getHost(),
                                            schemaRegistry.getMappedPort(8085)));
            dynamicProperty.add(
                    "spring.kafka.properties.schema.registry.url",
                    () ->
                            "http://%s:%d"
                                    .formatted(
                                            schemaRegistry.getHost(),
                                            schemaRegistry.getMappedPort(8085)));
        };
    }
}
