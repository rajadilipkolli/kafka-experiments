package com.example.boot.kafka.reactor.common;

import static com.example.boot.kafka.reactor.util.AppConstants.HELLO_TOPIC;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

@TestConfiguration(proxyBeanMethods = false)
public class TestKafkaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestKafkaConsumer.class);

    @Bean
    KafkaReceiver<Integer, MessageDTO> receiver(KafkaProperties properties) {
        LOGGER.info("Creating receiver");
        ReceiverOptions<Integer, MessageDTO> receiverOptions = ReceiverOptions.<Integer, MessageDTO>create(
                        properties.buildConsumerProperties())
                .subscription(Collections.singleton(HELLO_TOPIC))
                .addAssignListener(partitions -> LOGGER.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> LOGGER.debug("onPartitionsRevoked {}", partitions));

        return KafkaReceiver.create(receiverOptions);
    }
}
