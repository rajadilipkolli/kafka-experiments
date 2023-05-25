package com.example.springbootkafka.multi.receiver;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.springbootkafka.multi.domain.SimpleMessage;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_2;

import java.util.concurrent.CountDownLatch;

@Component
public class JsonReceiver {

    private static final Logger logger = LoggerFactory.getLogger(JsonReceiver.class);

    private final CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @KafkaListener(topics = TOPIC_TEST_2, containerFactory = "jsonKafkaListenerContainerFactory")
    public void listen2(ConsumerRecord<Integer, SimpleMessage> cr) {
        logger.info(TOPIC_TEST_2+" Received a message with key="+cr.key()+", value="+cr.value());
        latch.countDown();
    }

}
