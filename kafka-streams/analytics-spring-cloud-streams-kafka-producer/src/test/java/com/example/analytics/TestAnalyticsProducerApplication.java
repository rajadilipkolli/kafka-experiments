/* Licensed under Apache-2.0 2023 */
package com.example.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestAnalyticsProducerApplication {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer(DynamicPropertyRegistry propertyRegistry) {
        KafkaContainer kafkaContainer =
                new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka").withTag("7.5.3"))
                        .withKraft();
        propertyRegistry.add("spring.kafka.bootstrapServers", kafkaContainer::getBootstrapServers);
        return kafkaContainer;
    }

    public static void main(String[] args) {
        SpringApplication.from(AnalyticsProducerApplication::main)
                .with(TestAnalyticsProducerApplication.class)
                .run(args);
    }
}
