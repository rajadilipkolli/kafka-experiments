package com.example.springbootkafkasample.sender;

import com.example.springbootkafkasample.dto.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Sender {

    private static final Logger logger = LoggerFactory.getLogger(Sender.class);

    private final KafkaTemplate<String, MessageDTO> template;

    public Sender(KafkaTemplate<String, MessageDTO> template) {
        this.template = template;
    }

    public void send(MessageDTO messageDTO) {
        this.template.send(messageDTO.topic(), UUID.randomUUID().toString(), messageDTO)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        handleSuccess(messageDTO.topic(), messageDTO.msg());
                    } else {
                        handleFailure(messageDTO.topic(), messageDTO.msg(), ex);
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
