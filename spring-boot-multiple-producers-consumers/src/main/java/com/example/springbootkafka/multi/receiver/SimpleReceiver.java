package com.example.springbootkafka.multi.receiver;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_1;

import java.util.concurrent.CountDownLatch;

@Component
public class SimpleReceiver {

    private static final Logger logger = LoggerFactory.getLogger(SimpleReceiver.class);

    private final CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @KafkaListener(topics = TOPIC_TEST_1, containerFactory = "simpleKafkaListenerContainerFactory")
    public void listen1(ConsumerRecord<Integer, String> cr) {
        logger.info(TOPIC_TEST_1+" Received a message with key="+cr.key()+", value="+cr.value());
        latch.countDown();
    }

}
