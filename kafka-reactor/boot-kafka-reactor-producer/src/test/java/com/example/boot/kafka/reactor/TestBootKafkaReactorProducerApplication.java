package com.example.boot.kafka.reactor;

import static com.example.boot.kafka.reactor.util.AppConstants.HELLO_TOPIC;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

@TestConfiguration(proxyBeanMethods = false)
class TestBootKafkaReactorProducerApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestBootKafkaReactorProducerApplication.class);

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer(DynamicPropertyRegistry dynamicPropertyRegistry) {
        KafkaContainer kafkaContainer =
                new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("3.8.0"));
        dynamicPropertyRegistry.add("spring.kafka.bootstrapServers", kafkaContainer::getBootstrapServers);
        return kafkaContainer;
    }

    @Bean
    KafkaReceiver<Integer, MessageDTO> receiver(KafkaProperties properties) {
        LOGGER.info("Creating receiver");
        ReceiverOptions<Integer, MessageDTO> receiverOptions = ReceiverOptions.<Integer, MessageDTO>create(
                        properties.buildConsumerProperties(null))
                .subscription(Collections.singleton(HELLO_TOPIC))
                .addAssignListener(partitions -> LOGGER.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> LOGGER.debug("onPartitionsRevoked {}", partitions));

        return KafkaReceiver.create(receiverOptions);
    }

    public static void main(String[] args) {
        SpringApplication.from(BootKafkaReactorProducerApplication::main)
                .with(TestBootKafkaReactorProducerApplication.class)
                .run(args);
    }
}
