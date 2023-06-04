package com.example.springbootkafkasample.sender;

import com.example.springbootkafkasample.dto.MessageDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
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
                    ProducerRecord<String, MessageDTO> producerRecord = result.getProducerRecord();
                    if (ex == null) {
                        logger.info("Sent msg={} to topic :{} with key = {}", producerRecord.value().toString(), producerRecord.topic(), producerRecord.key());
                    } else {
                        MessageDTO messageDTO1 = producerRecord.value();
                        logger.error("Unable to send msg = {} to topic:{}", messageDTO1.msg(), producerRecord.topic(), ex);
                    }
                });
    }
}
