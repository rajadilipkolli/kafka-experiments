package com.example.springbootkafkaavro;

import com.example.springbootkafkaavro.containers.KafkaRaftWithExtraListenersContainer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

@TestConfiguration(proxyBeanMethods = false)
public class TestSpringBootKafkaAvroProducerApplication {

    private static final String KAFKA_NETWORK = "kafka-network";

    Network network = getNetwork();

    static Network getNetwork() {
        Network defaultDaprNetwork =
                new Network() {
                    @Override
                    public String getId() {
                        return KAFKA_NETWORK;
                    }

                    @Override
                    public void close() {}

                    @Override
                    public Statement apply(Statement base, Description description) {
                        return null;
                    }
                };

        List<com.github.dockerjava.api.model.Network> networks =
                DockerClientFactory.instance()
                        .client()
                        .listNetworksCmd()
                        .withNameFilter(KAFKA_NETWORK)
                        .exec();
        if (networks.isEmpty()) {
            Network.builder()
                    .createNetworkCmdModifier(cmd -> cmd.withName(KAFKA_NETWORK))
                    .build()
                    .getId();
        }
        return defaultDaprNetwork;
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaRaftWithExtraListenersContainer("confluentinc/cp-kafka:7.7.0")
                .withAdditionalListener(() -> "kafka:19092")
                .withKraft()
                .withNetwork(network)
                .withNetworkAliases("kafka")
                .withReuse(true);
    }

    @Bean
    @DependsOn("kafkaContainer")
    GenericContainer<?> schemaregistry(DynamicPropertyRegistry dynamicPropertyRegistry) {
        GenericContainer<?> schemaRegistry =
                new GenericContainer<>("confluentinc/cp-schema-registry:7.7.0")
                        .withExposedPorts(8085)
                        .withNetworkAliases("schemaregistry")
                        .withNetwork(network)
                        .withEnv(
                                "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
                                "PLAINTEXT://kafka:19092")
                        .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8085")
                        .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schemaregistry")
                        .withEnv("SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL", "PLAINTEXT")
                        .waitingFor(Wait.forHttp("/subjects"))
                        .withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS))
                        .withLabel("com.testcontainers.desktop.service", "cp-schema-registry");
        dynamicPropertyRegistry.add(
                "spring.kafka.producer.properties.schema.registry.url",
                () ->
                        "http://%s:%d"
                                .formatted(
                                        schemaRegistry.getHost(),
                                        schemaRegistry.getMappedPort(8085)));
        dynamicPropertyRegistry.add(
                "spring.kafka.properties.schema.registry.url",
                () ->
                        "http://%s:%d"
                                .formatted(
                                        schemaRegistry.getHost(),
                                        schemaRegistry.getMappedPort(8085)));

        return schemaRegistry;
    }

    public static void main(String[] args) {
        SpringApplication.from(SpringBootKafkaAvroProducerApplication::main)
                .with(TestSpringBootKafkaAvroProducerApplication.class)
                .run(args);
    }
}
