package com.example.springbootkafka.multi.receiver;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_2;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import jakarta.validation.Valid;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Component
@Slf4j
@Validated
public class JsonReceiver {

    private final CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = TOPIC_TEST_2, containerFactory = "jsonKafkaListenerContainerFactory")
    public void jsonListener(@Payload @Valid SimpleMessage cr) {
        log.info("{} Received a message with key = {} , value={}", TOPIC_TEST_2, cr.key(), cr.value());
        latch.countDown();
    }
}
