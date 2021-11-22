package com.sivalabs.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageSender {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String key, String value) {
        kafkaTemplate.send(KafkaConfig.TOPIC, key, value);
    }
}
