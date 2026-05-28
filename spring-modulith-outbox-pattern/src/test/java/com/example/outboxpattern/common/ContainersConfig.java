package com.example.outboxpattern.common;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("4.3.0"));
    }

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(KafkaContainer kafkaContainer) {
        return registry -> {
            // Connect our Spring application to our Testcontainers Kafka instance
            registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
            registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        };
    }

    @Bean
    @ServiceConnection
    LgtmStackContainer lgtmContainer() {
        return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.28.0"))
                .withStartupTimeout(Duration.ofMinutes(2))
                .withReuse(true);
    }
}
