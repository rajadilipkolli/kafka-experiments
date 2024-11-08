package com.example.boot.kafka.reactor.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("17.0-alpine"));
    }

    @ServiceConnection
    @Bean
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("3.9.0"));
    }

    @Bean
    DynamicPropertyRegistrar kafkaProperties(KafkaContainer kafkaContainer) {
        return (properties) -> {
            properties.add("spring.kafka.bootstrapServers", kafkaContainer::getBootstrapServers);
        };
    }
}
