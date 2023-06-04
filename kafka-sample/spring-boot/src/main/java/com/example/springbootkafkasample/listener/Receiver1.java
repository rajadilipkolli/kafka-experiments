package com.example.springbootkafkasample.listener;

import com.example.springbootkafkasample.dto.MessageDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import static com.example.springbootkafkasample.SpringBootKafkaSampleApplication.TOPIC_TEST_1;
import static com.example.springbootkafkasample.SpringBootKafkaSampleApplication.TOPIC_TEST_2;

@Component
public class Receiver1 {

    private static final Logger logger = LoggerFactory.getLogger(Receiver1.class);

    @KafkaListener(topics = TOPIC_TEST_1, groupId = "foo")
    @SendTo(TOPIC_TEST_2)
    public MessageDTO listen(ConsumerRecord<String, MessageDTO> cr) {
        logger.info(TOPIC_TEST_1 + " Received: {}", cr.toString());
        return cr.value();
    }

}
