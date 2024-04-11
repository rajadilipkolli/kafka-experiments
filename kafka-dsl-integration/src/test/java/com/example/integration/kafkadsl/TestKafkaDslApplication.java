package com.example.integration.kafkadsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestKafkaDslApplication {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer(DynamicPropertyRegistry dynamicPropertyRegistry) {
        KafkaContainer kafkaContainer = new KafkaContainer(
                        DockerImageName.parse("confluentinc/cp-kafka").withTag("7.6.0"))
                .withKraft();
        // Connect our Spring application to our Testcontainers Kafka instance
        dynamicPropertyRegistry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        return kafkaContainer;
    }

    public static void main(String[] args) {
        SpringApplication.from(KafkaDslApplication::main)
                .with(TestKafkaDslApplication.class)
                .run(args);
    }
}
