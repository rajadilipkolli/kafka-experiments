package com.example.springbootkafka.multi.receiver;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.example.springbootkafka.multi.domain.SimpleMessage;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_2;

import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
@Validated
public class JsonReceiver {

    private final CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @KafkaListener(topics = TOPIC_TEST_2, containerFactory = "jsonKafkaListenerContainerFactory")
    public void listen2(@Payload @Valid SimpleMessage cr) {
        log.info(TOPIC_TEST_2 + " Received a message with key=" + cr.key() + ", value=" + cr.value());
        latch.countDown();
    }
}
