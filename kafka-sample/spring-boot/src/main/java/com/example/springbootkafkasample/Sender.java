package com.example.springbootkafkasample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Sender {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootKafkaSampleApplication.class);

    private final KafkaTemplate<String, String> template;

    public Sender(KafkaTemplate<String, String> template) {
        this.template = template;
    }

    void send(String topic, String msg) {
        this.template.send(topic, UUID.randomUUID().toString(), msg)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        handleSuccess(topic, msg);
                    } else {
                        handleFailure(topic, msg, ex);
                    }
                });
    }

    private void handleFailure(String topic, String msg, Throwable ex) {
        logger.error("Unable to send msg = {} to topic:{}", msg, topic, ex);
    }

    private void handleSuccess(String topic, String msg) {
        logger.info("Sent msg={} to topic:{}", msg, topic);
    }
}
