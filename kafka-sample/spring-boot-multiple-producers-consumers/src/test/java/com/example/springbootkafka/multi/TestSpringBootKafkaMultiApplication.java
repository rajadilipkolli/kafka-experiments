package com.example.springbootkafka.multi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestSpringBootKafkaMultiApplication {

    private static final KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka").withTag("7.5.0")).withKraft();

    static {
        kafkaContainer.start();
        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
    }

    public static void main(String[] args) {
        SpringApplication.from(SpringBootKafkaMultiApplication::main)
                .with(TestSpringBootKafkaMultiApplication.class)
                .run(args);
    }
}
