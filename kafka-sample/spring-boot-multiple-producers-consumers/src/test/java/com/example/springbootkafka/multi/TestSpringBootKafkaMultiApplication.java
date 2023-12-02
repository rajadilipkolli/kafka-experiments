package com.example.springbootkafka.multi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestSpringBootKafkaMultiApplication {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka").withTag("7.5.1")).withKraft();
    }

    public static void main(String[] args) {
        SpringApplication.from(SpringBootKafkaMultiApplication::main)
                .with(TestSpringBootKafkaMultiApplication.class)
                .run(args);
    }
}
