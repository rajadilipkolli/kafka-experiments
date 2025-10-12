package com.example.springbootkafkasample.common;

import org.apache.kafka.clients.admin.NewTopic;
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
                new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("4.1.0"));
        kafkaContainer.addEnv("KAFKA_NUM_PARTITIONS", "32");
        return kafkaContainer;
    }

    @Bean
    NewTopic test2Topic() {
        return new NewTopic("test_2", 32, (short) 1);
    }
}
