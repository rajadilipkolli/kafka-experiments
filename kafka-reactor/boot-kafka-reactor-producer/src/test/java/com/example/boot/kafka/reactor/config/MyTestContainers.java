package com.example.boot.kafka.reactor.config;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.util.AppConstants;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

@TestConfiguration(proxyBeanMethods = false)
@Slf4j
public class MyTestContainers {

    @Bean
    @ServiceConnection
    @RestartScope
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.3-alpine"));
    }

    @Bean
    @ServiceConnection
    @RestartScope
    KafkaContainer kafkaContainer(DynamicPropertyRegistry propertyRegistry) {
        KafkaContainer kafkaContainer = new KafkaContainer(
                        DockerImageName.parse("confluentinc/cp-kafka").withTag("7.4.0"))
                .withKraft();
        propertyRegistry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        propertyRegistry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        return kafkaContainer;
    }

    @Bean
    @Lazy
    KafkaReceiver<Integer, MessageDTO> receiver(KafkaProperties properties) {
        ReceiverOptions<Integer, MessageDTO> receiverOptions = ReceiverOptions.<Integer, MessageDTO>create(
                        properties.buildConsumerProperties())
                .subscription(Collections.singleton(AppConstants.HELLO_TOPIC))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));

        return KafkaReceiver.create(receiverOptions);
    }
}
