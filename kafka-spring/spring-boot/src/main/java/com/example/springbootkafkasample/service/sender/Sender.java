package com.example.springbootkafkasample.service.sender;

import com.example.springbootkafkasample.dto.MessageDTO;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class Sender {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    private final KafkaTemplate<UUID, MessageDTO> template;

    public Sender(KafkaTemplate<UUID, MessageDTO> template) {
        this.template = template;
    }

    public void send(MessageDTO messageDTO) {
        this.template.send(messageDTO.topic(), UUID.randomUUID(), messageDTO).whenComplete((result, ex) -> {
            if (ex == null) {
                LOGGER.info("Sent message=[" + messageDTO + "] with offset=["
                        + result.getRecordMetadata().offset() + "]");
            } else {
                LOGGER.warn("Unable to send message=[" + messageDTO + "] due to : " + ex.getMessage());
            }
        });
    }
}
