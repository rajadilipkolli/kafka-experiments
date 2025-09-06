package com.example.integration.kafkadsl;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class ContainerConfiguration {

    @ServiceConnection
    @Bean
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("4.1.0"));
    }

    @Bean
    DynamicPropertyRegistrar kafkaProperties(KafkaContainer kafkaContainer) {
        return properties -> properties.add("spring.kafka.bootstrapServers", kafkaContainer::getBootstrapServers);
    }
}
