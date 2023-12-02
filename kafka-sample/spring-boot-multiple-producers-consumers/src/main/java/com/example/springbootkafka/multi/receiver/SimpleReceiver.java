package com.example.springbootkafka.multi.receiver;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_1;

import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Getter
@Slf4j
@Component
public class SimpleReceiver {

    private final CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = TOPIC_TEST_1, containerFactory = "simpleKafkaListenerContainerFactory")
    public void simpleListener(ConsumerRecord<Integer, String> cr) {
        log.info(TOPIC_TEST_1 + " Received a message with key=" + cr.key() + ", value=" + cr.value());
        latch.countDown();
    }
}
