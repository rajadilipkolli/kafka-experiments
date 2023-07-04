package com.example.springbootkafkasample.service.listener;

import static com.example.springbootkafkasample.config.Initializer.TOPIC_TEST_1;
import static com.example.springbootkafkasample.config.Initializer.TOPIC_TEST_2;

import com.example.springbootkafkasample.dto.MessageDTO;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class Receiver1 {

    private static final Logger logger = LoggerFactory.getLogger(Receiver1.class);

    @KafkaListener(topics = TOPIC_TEST_1, groupId = "foo", errorHandler = "validationErrorHandler")
    @SendTo(TOPIC_TEST_2)
    public MessageDTO listen(@Header(KafkaHeaders.RECEIVED_KEY) UUID key, ConsumerRecord<String, MessageDTO> cr) {
        logger.info("Received message : {} with Key :{} in topic :{}", cr.toString(), key, cr.topic());
        return cr.value();
    }
}
