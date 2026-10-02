package com.example.springbootkafka.multi.common;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfiguration {

    /** Creates a reusable LGTM observability container with a two-minute startup timeout. */
    @Bean
    @ServiceConnection
    LgtmStackContainer lgtmContainer() {
        return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.34.0"))
                .withStartupTimeout(Duration.ofMinutes(2))
                .withReuse(true);
    }

    /** Creates the Kafka broker container used by the integration tests. */
    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("4.3.1"));
    }

    /** Registers the test broker bootstrap address in the Spring Kafka configuration. */
    @Bean
    DynamicPropertyRegistrar kafkaProperties(KafkaContainer kafkaContainer) {
        return (properties) -> {
            // Connect our Spring application to our Testcontainers Kafka instance
            properties.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
            properties.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        };
    }
}
