package com.example.springbootkafkaavro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@SpringBootTest(
        properties = {
            "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
            "spring.kafka.consumer.value-deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer",
            "spring.kafka.consumer.group-id=group-1"
        })
@AutoConfigureMockMvc
@Import(AvroKafkaListener.class)
@ExtendWith(OutputCaptureExtension.class)
class SpringBootKafkaAvroProducerApplicationTests {

    @Autowired MockMvc mockMvc;

    private static final Network KAFKA_NETWORK = Network.newNetwork();
    private static final String CONFLUENT_PLATFORM_VERSION = "7.4.0";
    private static final DockerImageName KAFKA_IMAGE =
            DockerImageName.parse("confluentinc/cp-kafka").withTag(CONFLUENT_PLATFORM_VERSION);
    private static final KafkaContainer KAFKA =
            new KafkaContainer(KAFKA_IMAGE)
                    .withNetwork(KAFKA_NETWORK)
                    .withKraft()
                    .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
                    .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1");

    private static final SchemaRegistryContainer SCHEMA_REGISTRY =
            new SchemaRegistryContainer(CONFLUENT_PLATFORM_VERSION)
                    .withStartupTimeout(Duration.ofMinutes(2));

    static {
        KAFKA.start();
        SCHEMA_REGISTRY.withKafka(KAFKA).start();
        // Should be set after container is started
        SCHEMA_REGISTRY.withEnv("SCHEMA_REGISTRY_LISTENERS", SCHEMA_REGISTRY.getSchemaUrl());
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Connect our Spring application to our Testcontainers Kafka instance
        registry.add("spring.kafka.consumer.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add(
                "spring.kafka.producer.properties.schema.registry.url",
                SCHEMA_REGISTRY::getSchemaUrl);
        registry.add("spring.kafka.properties.schema.registry.url", SCHEMA_REGISTRY::getSchemaUrl);
    }

    private static class SchemaRegistryContainer extends GenericContainer<SchemaRegistryContainer> {
        public static final String SCHEMA_REGISTRY_IMAGE = "confluentinc/cp-schema-registry";
        public static final int SCHEMA_REGISTRY_PORT = 8081;

        public SchemaRegistryContainer() {
            this(CONFLUENT_PLATFORM_VERSION);
        }

        public SchemaRegistryContainer(String version) {
            super(DockerImageName.parse(SCHEMA_REGISTRY_IMAGE).withTag(CONFLUENT_PLATFORM_VERSION));

            waitingFor(Wait.forHttp("/subjects").forStatusCode(200));
            withExposedPorts(SCHEMA_REGISTRY_PORT);
        }

        public SchemaRegistryContainer withKafka(KafkaContainer kafka) {
            return withKafka(kafka.getNetwork(), kafka.getNetworkAliases().get(0) + ":9092");
        }

        public SchemaRegistryContainer withKafka(Network network, String bootstrapServers) {
            withNetwork(network);
            withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry");
            withEnv(
                    "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
                    "PLAINTEXT://" + bootstrapServers);
            return self();
        }

        public String getSchemaUrl() {
            return String.format("http://%s:%d", getHost(), getMappedPort(SCHEMA_REGISTRY_PORT));
        }
    }

    @Test
    void contextLoads(CapturedOutput output) throws Exception {
        this.mockMvc
                .perform(post("/person/publish").param("name", "junit").param("age", "33"))
                .andExpect(status().isOk());
        await().atMost(30, SECONDS)
                .untilAsserted(
                        () -> assertThat(output.getOut()).contains("Person received : junit : 33"));
    }
}
