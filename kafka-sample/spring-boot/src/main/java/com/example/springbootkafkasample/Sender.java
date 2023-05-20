package com.example.springbootkafkasample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class Sender {

    private static Logger logger = LoggerFactory.getLogger(SpringBootKafkaSampleApplication.class);

    private final KafkaTemplate<String, String> template;

    public Sender(KafkaTemplate<String, String> template) {
        this.template = template;
    }

    void send(String topic, String msg) {
        this.template.send(topic, msg);
        logger.info("Sent msg={} to topic:{}",msg, topic);
    }
}
