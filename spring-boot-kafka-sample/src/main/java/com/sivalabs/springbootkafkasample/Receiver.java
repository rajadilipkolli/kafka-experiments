package com.sivalabs.springbootkafkasample;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

import static com.sivalabs.springbootkafkasample.SpringBootKafkaSampleApplication.TOPIC_TEST_1;
import static com.sivalabs.springbootkafkasample.SpringBootKafkaSampleApplication.TOPIC_TEST_2;

@Component
public class Receiver {

    private static Logger logger = LoggerFactory.getLogger(Receiver.class);

    private CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @KafkaListener(topics = TOPIC_TEST_1)
    @SendTo(TOPIC_TEST_2)
    public String listen(ConsumerRecord<?, ?> cr) {
        logger.info(TOPIC_TEST_1+" Received: "+cr.toString());
        return String.valueOf(cr.value());
    }

    @KafkaListener(topics = TOPIC_TEST_2)
    public void listenTopic2(ConsumerRecord<?, ?> cr) {
        logger.info(TOPIC_TEST_2+" Received: "+cr.toString());
        latch.countDown();
    }

}
