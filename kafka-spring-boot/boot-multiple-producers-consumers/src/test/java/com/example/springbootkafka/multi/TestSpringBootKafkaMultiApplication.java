package com.example.springbootkafka.multi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestSpringBootKafkaMultiApplication {

    @Bean
    @ServiceConnection(name = "openzipkin/zipkin")
    GenericContainer<?> zipkinContainer() {
        return new GenericContainer<>(DockerImageName.parse("openzipkin/zipkin:latest")).withExposedPorts(9411);
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer(DynamicPropertyRegistry dynamicPropertyRegistry) {
        KafkaContainer kafkaContainer =
                new KafkaContainer(DockerImageName.parse("apache/kafka").withTag("3.8.0")).withReuse(true);
        // Connect our Spring application to our Testcontainers Kafka instance
        dynamicPropertyRegistry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        dynamicPropertyRegistry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        return kafkaContainer;
    }

    public static void main(String[] args) {
        SpringApplication.from(SpringBootKafkaMultiApplication::main)
                .with(TestSpringBootKafkaMultiApplication.class)
                .run(args);
    }
}
