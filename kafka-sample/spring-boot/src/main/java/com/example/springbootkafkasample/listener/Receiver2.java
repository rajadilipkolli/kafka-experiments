package com.example.springbootkafkasample.listener;

import static com.example.springbootkafkasample.SpringBootKafkaSampleApplication.TOPIC_TEST_2;

import com.example.springbootkafkasample.dto.MessageDTO;
import jakarta.validation.Valid;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class Receiver2 {

    private static final Logger logger = LoggerFactory.getLogger(Receiver2.class);

    private final CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @KafkaListener(topics = TOPIC_TEST_2, errorHandler = "validationErrorHandler")
    public void listenTopic2(@Payload @Valid MessageDTO messageDTO) {
        logger.info(TOPIC_TEST_2 + " Received: {}", messageDTO.toString());
        latch.countDown();
    }
}
