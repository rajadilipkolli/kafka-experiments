package com.example.springbootkafkasample;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

import static com.example.springbootkafkasample.SpringBootKafkaSampleApplication.TOPIC_TEST_2;

@Component
public class Receiver2 implements ConsumerSeekAware {

    private static final Logger logger = LoggerFactory.getLogger(Receiver2.class);


    private final CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @KafkaListener(topics = TOPIC_TEST_2)
    public void listenTopic2(ConsumerRecord<String, String> cr) {
        logger.info(TOPIC_TEST_2 + " Received: " + cr.toString());
        latch.countDown();
    }
}
