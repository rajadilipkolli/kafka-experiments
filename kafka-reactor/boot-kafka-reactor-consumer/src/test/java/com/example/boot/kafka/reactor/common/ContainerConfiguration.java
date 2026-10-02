package com.example.boot.kafka.reactor.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfiguration {

    /** Creates the PostgreSQL container used to persist messages during integration tests. */
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres").withTag("18.6-alpine"));
    }

    /** Creates the Kafka broker container used by the integration tests. */
    @ServiceConnection
    @Bean
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("4.3.1"));
    }

    /** Registers the test broker bootstrap address in the Spring Kafka configuration. */
    @Bean
    DynamicPropertyRegistrar kafkaProperties(KafkaContainer kafkaContainer) {
        return (properties) -> {
            properties.add("spring.kafka.bootstrapServers", kafkaContainer::getBootstrapServers);
        };
    }
}
