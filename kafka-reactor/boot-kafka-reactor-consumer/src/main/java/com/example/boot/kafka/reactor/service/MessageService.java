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
import org.springframework.dao.DataIntegrityViolationException;
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

    /** Creates the message service with its persistence repository. */
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Persists an event using its supplied ID or an ID derived from the Kafka topic, partition,
     * and offset. Constraint violations complete successfully only if that event ID already exists.
     *
     * @param key the received Kafka key, used for logging
     * @param consumerRecord the message and its Kafka metadata
     * @return completion after persistence or duplicate detection, or an error for other failures
     */
    @KafkaListener(topics = AppConstants.HELLO_TOPIC, groupId = "reactivekafka")
    Mono<Void> listen(
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

        MessageDTO messageDTO = consumerRecord.value();
        java.util.UUID eventId = messageDTO.eventId();
        if (eventId == null) {
            String name = consumerRecord.topic() + "-" + consumerRecord.partition() + "-" + consumerRecord.offset();
            eventId = java.util.UUID.nameUUIDFromBytes(name.getBytes());
        }

        MessageDTO toSave = new MessageDTO(null, messageDTO.text(), messageDTO.sentAt(), eventId);

        return messageRepository
                .save(toSave)
                .then()
                .onErrorResume(
                        DataIntegrityViolationException.class,
                        e -> messageRepository.existsByEventId(toSave.eventId()).flatMap(exists -> {
                            if (exists) {
                                log.debug("Duplicate event ignored: {}", toSave.eventId());
                                return Mono.empty();
                            }
                            return Mono.error(e);
                        }));
    }

    /** Returns all stored messages, logging each result and any retrieval error. */
    public Flux<MessageDTO> fetchMessages() {
        return messageRepository
                .findAll()
                .doOnNext(messageDTO -> log.info("Retrieved Message :{}", messageDTO))
                .doOnError(e -> log.error("Reading Error ", e));
    }
}
