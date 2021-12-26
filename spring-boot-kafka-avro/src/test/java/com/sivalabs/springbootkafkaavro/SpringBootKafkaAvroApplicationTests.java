package com.sivalabs.springbootkafkaavro;

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

@SpringBootTest
@Testcontainers
public class SpringBootKafkaAvroApplicationTests {

  private static final DockerImageName KAFKA_TEST_IMAGE =
      DockerImageName.parse("confluentinc/cp-kafka:6.1.0");

  private static final DockerImageName KAFKA_SCHEMA_IMAGE =
      DockerImageName.parse("confluentinc/cp-schema-registry:6.1.0");

  private static final Duration startupTimeout = Duration.ofMinutes(5);

  @Container
  public static final KafkaContainer KAFKA =
      new KafkaContainer(KAFKA_TEST_IMAGE).withStartupTimeout(startupTimeout);

  @Container
  public static final GenericContainer KAFKA_SCHEMA =
      new GenericContainer(KAFKA_SCHEMA_IMAGE)
          .withExposedPorts(8081)
          .withEnv("SCHEMA_REGISTRY_HOST_NAME", "0.0.0.0")
          .withEnv(
              "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
              "PLAINTEXT://0.0.0.0:9093,BROKER://0.0.0.0:9092")
          .withStartupTimeout(startupTimeout);

  static {
    Startables.deepStart(Stream.of(KAFKA, KAFKA_SCHEMA)).join();
  }

  @DynamicPropertySource
  static void registerPgProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.kafka.consumer.bootstrap-servers", () -> KAFKA.getBootstrapServers().substring(12));
    registry.add(
        "spring.kafka.producer.bootstrap-servers", () -> KAFKA.getBootstrapServers().substring(12));
    registry.add(
        "spring.kafka.properties.schema.registry.url",
        () -> KAFKA_SCHEMA.getHost() + ":" + KAFKA_SCHEMA.getMappedPort(8081));
    registry.add(
        "spring.kafka.producer.properties.schema.registry.url",
        () -> KAFKA_SCHEMA.getHost() + ":" + KAFKA_SCHEMA.getMappedPort(8081));
  }

  @Test
  void contextLoads() {
    assertThat(KAFKA.isRunning()).isTrue();
  }
}
