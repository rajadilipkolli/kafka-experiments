package com.sivalabs.springbootkafka.multi.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.sivalabs.springbootkafka.multi.domain.SimpleMessage;

import static com.sivalabs.springbootkafka.multi.util.AppConstants.TOPIC_TEST_1;
import static com.sivalabs.springbootkafka.multi.util.AppConstants.TOPIC_TEST_2;

@Component
public class Sender {

    private static final Logger logger = LoggerFactory.getLogger(Sender.class);

    @Autowired
    private KafkaTemplate<Integer, String> simpleKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, SimpleMessage> jsonKafkaTemplate;

    public void send(Integer key, String msg) {
        this.simpleKafkaTemplate.send(TOPIC_TEST_1, key, msg);
        logger.info("Sent key= {}, msg= {} to topic: {}",key,msg,TOPIC_TEST_1);
    }

    public void send(SimpleMessage msg) {
        String key = String.valueOf(msg.key());
        this.jsonKafkaTemplate.send(TOPIC_TEST_2, key, msg);
        logger.info("Sent key= {}, msg= {} to topic: {}",key,msg,TOPIC_TEST_2);
    }
}
