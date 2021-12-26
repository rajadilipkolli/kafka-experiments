package com.sivalabs.springbootkafkaavro;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
public class SpringBootKafkaAvroApplicationTests {

    private static final DockerImageName KAFKA_TEST_IMAGE =
    DockerImageName.parse("confluentinc/cp-kafka:5.3.6-1");

    @Container public static final KafkaContainer KAFKA = new KafkaContainer(KAFKA_TEST_IMAGE);

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
      registry.add("spring.kafka.properties.schema.registry.url", () -> KAFKA.getBootstrapServers().substring(12));
      registry.add("spring.kafka.producer.properties.schema.registry.url", () -> KAFKA.getBootstrapServers().substring(12));
    
    }

    @Test
    void contextLoads() {
      assertThat(KAFKA.isRunning()).isTrue();
    }

}
