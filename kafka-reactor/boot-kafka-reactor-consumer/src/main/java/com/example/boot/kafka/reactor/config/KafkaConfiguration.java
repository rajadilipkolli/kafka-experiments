package com.example.boot.kafka.reactor.config;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.util.AppConstants;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

@EnableKafka
@Configuration(proxyBeanMethods = false)
@Slf4j
public class KafkaConfiguration {

    @Bean
    NewTopic helloTopic() {
        log.info("Creating helloTopic");
        return new NewTopic(AppConstants.HELLO_TOPIC, 1, (short) 1);
    }

    @Bean
    KafkaReceiver<Integer, MessageDTO> receiver(KafkaProperties properties) {
        log.info("Creating receiver");
        ReceiverOptions<Integer, MessageDTO> receiverOptions = ReceiverOptions.<Integer, MessageDTO>create(
                        properties.buildConsumerProperties())
                .subscription(Collections.singleton(AppConstants.HELLO_TOPIC))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));

        return KafkaReceiver.create(receiverOptions);
    }
}
