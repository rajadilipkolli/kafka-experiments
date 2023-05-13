/* Licensed under Apache-2.0 2021-2022 */
package com.example.analytics.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class KafkaTestContainersConfiguration {

    private static final DockerImageName KAFKA_TEST_IMAGE =
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0");

    @Bean
    @ServiceConnection
    public static final KafkaContainer kafkaContainer(DynamicPropertyRegistry registry) {
        KafkaContainer kafkaContainer = new KafkaContainer(KAFKA_TEST_IMAGE).withKraft();
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add(
                "spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add(
                "spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        return kafkaContainer;
    }
}
