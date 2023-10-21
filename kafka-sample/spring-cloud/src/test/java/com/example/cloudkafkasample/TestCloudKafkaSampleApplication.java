/* (C)2023 */
package com.example.cloudkafkasample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestCloudKafkaSampleApplication {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer(DynamicPropertyRegistry dynamicPropertyRegistry) {
        KafkaContainer kafkaContainer = new KafkaContainer(
                        DockerImageName.parse("confluentinc/cp-kafka").withTag("7.5.1"))
                .withKraft();
        dynamicPropertyRegistry.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);
        return kafkaContainer;
    }

    public static void main(String[] args) {
        SpringApplication.from(CloudKafkaSampleApplication::main)
                .with(TestCloudKafkaSampleApplication.class)
                .run(args);
    }
}
