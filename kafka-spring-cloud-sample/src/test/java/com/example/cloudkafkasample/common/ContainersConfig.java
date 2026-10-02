/* (C)2024 */
package com.example.cloudkafkasample.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    /** Creates the Kafka broker container used by the integration tests. */
    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("4.3.1")).withReuse(true);
    }

    /** Registers the test broker address for the Spring Cloud Stream Kafka binder. */
    @Bean
    DynamicPropertyRegistrar kafkaProperties(KafkaContainer kafkaContainer) {
        return (properties) -> {
            // Connect our Spring application to our Testcontainers Kafka instance
            properties.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);
        };
    }
}
