package com.example.boot.kafka.reactor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestBootKafkaReactorProducerApplication {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka").withTag("7.4.0")).withKraft();
    }

	public static void main(String[] args) {
		SpringApplication.from(BootKafkaReactorProducerApplication::main).with(TestBootKafkaReactorProducerApplication.class).run(args);
	}

}