package com.example.boot.kafka.reactor;

import static com.example.boot.kafka.reactor.util.AppConstants.HELLO_TOPIC;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import java.util.Collections;
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
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

@TestConfiguration(proxyBeanMethods = false)
@Slf4j
public class TestBootKafkaReactorProducerApplication {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.3-alpine"));
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer(DynamicPropertyRegistry propertyRegistry) {
        KafkaContainer kafkaContainer = new KafkaContainer(
                        DockerImageName.parse("confluentinc/cp-kafka").withTag("7.4.1"))
                .withKraft();
        propertyRegistry.add("spring.kafka.bootstrapServers", kafkaContainer::getBootstrapServers);
        return kafkaContainer;
    }

    @Bean
    KafkaReceiver<Integer, MessageDTO> receiver(KafkaProperties properties) {
        log.info("Creating receiver");
        ReceiverOptions<Integer, MessageDTO> receiverOptions = ReceiverOptions.<Integer, MessageDTO>create(
                        properties.buildConsumerProperties())
                .subscription(Collections.singleton(HELLO_TOPIC))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));

        return KafkaReceiver.create(receiverOptions);
    }

    public static void main(String[] args) {
        SpringApplication.from(BootKafkaReactorProducerApplication::main)
                .with(TestBootKafkaReactorProducerApplication.class)
                .run(args);
    }
}
