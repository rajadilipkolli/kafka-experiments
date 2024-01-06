package com.example.boot.kafka.reactor;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@TestConfiguration(proxyBeanMethods = false)
@Slf4j
public class TestBootKafkaReactorConsumerApplication {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.1-alpine"));
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer(DynamicPropertyRegistry propertyRegistry) {
        KafkaContainer kafkaContainer = new KafkaContainer(
                        DockerImageName.parse("confluentinc/cp-kafka").withTag("7.5.3"))
                .withKraft();
        propertyRegistry.add("spring.kafka.bootstrapServers", kafkaContainer::getBootstrapServers);
        return kafkaContainer;
    }

    @Bean
    KafkaSender<Integer, MessageDTO> reactiveKafkaSender(KafkaProperties properties) {
        log.info("Creating Sender");
        Map<String, Object> props = properties.buildProducerProperties();
        return KafkaSender.create(SenderOptions.create(props));
    }

    public static void main(String[] args) {
        SpringApplication.from(BootKafkaReactorConsumerApplication::main)
                .with(TestBootKafkaReactorConsumerApplication.class)
                .run(args);
    }
}
