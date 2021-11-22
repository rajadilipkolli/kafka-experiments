package com.sivalabs.springbootkafka.multi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.sivalabs.springbootkafka.multi.SpringBootKafkaMultiApplication.TOPIC_TEST_1;
import static com.sivalabs.springbootkafka.multi.SpringBootKafkaMultiApplication.TOPIC_TEST_2;

@Component
public class Sender {

    private static final Logger logger = LoggerFactory.getLogger(Sender.class);

    @Autowired
    private KafkaTemplate<Integer, String> simpleKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, SimpleMessage> jsonKafkaTemplate;

    void send(Integer key, String msg) {
        this.simpleKafkaTemplate.send(TOPIC_TEST_1, key, msg);
        logger.info("Sent key="+key+", msg="+msg+" to topic:"+TOPIC_TEST_1);
    }

    void send(SimpleMessage msg) {
        String key = String.valueOf(msg.getKey());
        this.jsonKafkaTemplate.send(TOPIC_TEST_2, key, msg);
        logger.info("Sent key="+key+", msg="+msg+" to topic:"+TOPIC_TEST_2);
    }
}
