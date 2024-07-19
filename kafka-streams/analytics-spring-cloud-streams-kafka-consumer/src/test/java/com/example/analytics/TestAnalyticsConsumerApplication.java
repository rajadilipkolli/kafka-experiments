/* Licensed under Apache-2.0 2023-2024 */
package com.example.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestAnalyticsConsumerApplication {

    private static final DockerImageName KAFKA_IMAGE_NAME =
            DockerImageName.parse("apache/kafka").withTag("3.7.1");

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer(DynamicPropertyRegistry registry) {
        KafkaContainer kafkaContainer = new KafkaContainer(KAFKA_IMAGE_NAME);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        return kafkaContainer;
    }

    public static void main(String[] args) {
        SpringApplication.from(AnalyticsConsumerApplication::main)
                .with(TestAnalyticsConsumerApplication.class)
                .run(args);
    }
}
