package com.example.boot.kafka.reactor.common;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

/**
 * Test configuration class that provides a reactive Kafka producer for integration testing.
 * This configuration creates a KafkaSender bean that can be used to send messages
 * in a reactive manner during tests.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(TestKafkaProducer.class);

    @Bean
    KafkaSender<Integer, MessageDTO> reactiveKafkaSender(KafkaProperties properties) {
        log.info("Creating reactive Kafka sender with properties: {}", properties.getProducer());
        Map<String, Object> props = properties.buildProducerProperties();
        SenderOptions<Integer, MessageDTO> senderOptions = SenderOptions.create(props);
        senderOptions.maxInFlight(1024);
        senderOptions.stopOnError(false);

        KafkaSender<Integer, MessageDTO> sender = KafkaSender.create(senderOptions);

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Closing Kafka sender");
            sender.close();
        }));

        return sender;
    }
}
