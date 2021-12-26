package com.sivalabs.springbootkafkaavro;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.KafkaContainer.KAFKA_PORT;

@SpringBootTest
@Testcontainers
public class SpringBootKafkaAvroApplicationTests {

  private static final DockerImageName KAFKA_TEST_IMAGE =
      DockerImageName.parse("confluentinc/cp-kafka:6.1.0");

  private static final Duration startupTimeout = Duration.ofMinutes(5);

  @Container
  public static final KafkaContainer KAFKA =
      new KafkaContainer(KAFKA_TEST_IMAGE).withStartupTimeout(startupTimeout);

  public static class SchemaRegistryContainer extends GenericContainer<SchemaRegistryContainer> {
    public SchemaRegistryContainer() {
      this("6.1.0");
    }

    public SchemaRegistryContainer(String version) {
      super("confluentinc/cp-schema-registry:" + version);
      withExposedPorts(8081);
    }

    public SchemaRegistryContainer withKafka(KafkaContainer kafka) {
      withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry");
      withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081");
      withEnv(
          "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
          "PLAINTEXT://" + kafka.getNetworkAliases().get(0) + ":9092");
      return self();
    }

    public String getSchemaRegistryUrl() {
      return "http://" + getContainerIpAddress() + ":" + getMappedPort(8081);
    }
  }

  private static final SchemaRegistryContainer KAFKA_SCHEMA =
      new SchemaRegistryContainer("6.1.0").withKafka(KAFKA).withStartupTimeout(startupTimeout);

  @BeforeAll
  static void startContainers() {
    Startables.deepStart(Stream.of(KAFKA, KAFKA_SCHEMA)).join();
  }

  @DynamicPropertySource
  static void registerPgProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.kafka.consumer.bootstrap-servers",
        () -> String.format("http://%s:%s", KAFKA.getHost(), KAFKA.getMappedPort(KAFKA_PORT)));
    registry.add(
        "spring.kafka.producer.bootstrap-servers",
        () -> String.format("http://%s:%s", KAFKA.getHost(), KAFKA.getMappedPort(KAFKA_PORT)));
    registry.add("spring.kafka.properties.schema.registry.url", KAFKA_SCHEMA::getSchemaRegistryUrl);
    registry.add(
        "spring.kafka.producer.properties.schema.registry.url", KAFKA_SCHEMA::getSchemaRegistryUrl);
  }

  @Test
  void contextLoads() {
    assertThat(KAFKA.isRunning()).isTrue();
    assertThat(KAFKA_SCHEMA.isRunning()).isTrue();
  }
}
