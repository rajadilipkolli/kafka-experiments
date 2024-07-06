package com.example.boot.kafka.reactor.service;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.util.AppConstants;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private final KafkaSender<Integer, MessageDTO> sender;

    public MessageService(KafkaSender<Integer, MessageDTO> sender) {
        this.sender = sender;
    }

    public void sendMessage(MessageDTO messageDTO) {
        Integer key = new SecureRandom().nextInt(Integer.MAX_VALUE);
        Flux<SenderRecord<Integer, MessageDTO, Integer>> outboundFlux =
                Flux.just(SenderRecord.create(new ProducerRecord<>(AppConstants.HELLO_TOPIC, key, messageDTO), key));
        this.sender
                .send(outboundFlux)
                .doOnError(e -> log.error("Send failed", e))
                .subscribe(r -> {
                    RecordMetadata metadata = r.recordMetadata();
                    log.info(
                            "Message {} sent successfully, topic-partition={}-{} offset={} timestamp={}",
                            r.correlationMetadata(),
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset(),
                            LocalDateTime.now());
                });
    }
}
