package com.example.boot.kafka.reactor.common;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@TestConfiguration(proxyBeanMethods = false)
public class TestKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(TestKafkaProducer.class);

    @Bean
    KafkaSender<Integer, MessageDTO> reactiveKafkaSender(KafkaProperties properties) {
        log.info("Creating Sender");
        Map<String, Object> props = properties.buildProducerProperties(null);
        return KafkaSender.create(SenderOptions.create(props));
    }
}
