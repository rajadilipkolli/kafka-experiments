package com.example.springbootkafkasample.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        KafkaContainer kafkaContainer =
                new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("3.8.1"));
        kafkaContainer.addEnv("KAFKA_NUM_PARTITIONS", "32");
        return kafkaContainer;
    }
}
