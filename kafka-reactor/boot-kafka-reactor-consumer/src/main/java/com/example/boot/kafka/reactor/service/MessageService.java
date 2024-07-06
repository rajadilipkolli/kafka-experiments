package com.example.boot.kafka.reactor.service;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.repository.MessageRepository;
import com.example.boot.kafka.reactor.util.AppConstants;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @KafkaListener(topics = AppConstants.HELLO_TOPIC, groupId = "reactivekafka")
    Mono<MessageDTO> listen(
            @Header(KafkaHeaders.RECEIVED_KEY) Integer key, ConsumerRecord<Integer, MessageDTO> consumerRecord) {
        ZonedDateTime zdt =
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(consumerRecord.timestamp()), ZoneId.systemDefault());
        log.info(
                "Received message: topic-partition={} offset={} timestamp={} key={} value={}",
                consumerRecord.partition(),
                consumerRecord.offset(),
                zdt,
                key,
                consumerRecord.value());
        return messageRepository.save(consumerRecord.value());
    }

    public Flux<MessageDTO> fetchMessages() {
        return messageRepository
                .findAll()
                .doOnNext(messageDTO -> log.info("Retrieved Message :{}", messageDTO))
                .doOnError(e -> log.error("Reading Error ", e));
    }
}
