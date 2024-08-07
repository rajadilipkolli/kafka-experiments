package com.example.springbootkafkasample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestBootKafkaSampleApplication {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        KafkaContainer kafkaContainer =
                new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("3.8.0"));
        kafkaContainer.addEnv("KAFKA_NUM_PARTITIONS", "32");
        return kafkaContainer;
    }

    public static void main(String[] args) {
        SpringApplication.from(BootKafkaSampleApplication::main)
                .with(TestBootKafkaSampleApplication.class)
                .run(args);
    }
}
