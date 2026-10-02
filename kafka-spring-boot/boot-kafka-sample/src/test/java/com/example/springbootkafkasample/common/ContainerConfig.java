package com.example.springbootkafkasample.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

    /** Creates the test Kafka broker with a default of 32 partitions per topic. */
    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        KafkaContainer kafkaContainer =
                new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("4.3.1"));
        kafkaContainer.addEnv("KAFKA_NUM_PARTITIONS", "32");
        return kafkaContainer;
    }
}
