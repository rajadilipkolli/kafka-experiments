package com.example.boot.kafka.reactor.service;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.util.AppConstants;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private final KafkaSender<Integer, MessageDTO> sender;
    private final Duration sendTimeout;

    /**
     * Creates the message service with its reactive Kafka sender.
     *
     * @param sender sender used to publish messages
     * @param sendTimeoutMs maximum duration of the subscribed send, in milliseconds
     */
    public MessageService(
            KafkaSender<Integer, MessageDTO> sender,
            @Value("${app.producer.send-timeout-ms:12000}") long sendTimeoutMs) {
        this.sender = sender;
        this.sendTimeout = Duration.ofMillis(sendTimeoutMs);
    }

    /**
     * Publishes a message on subscription, assigning a random event ID only when one is absent.
     * The supplied event ID is preserved so callers can reuse it for repeated deliveries.
     *
     * @param messageDTO the message to publish
     * @return completion without a value after a successful send result, propagating sender or
     *     result errors, or signaling {@link java.util.concurrent.TimeoutException} if the
     *     configured duration expires
     */
    public Mono<Void> sendMessage(MessageDTO messageDTO) {
        MessageDTO toSend = messageDTO.eventId() == null
                ? new MessageDTO(messageDTO.id(), messageDTO.text(), messageDTO.sentAt(), java.util.UUID.randomUUID())
                : messageDTO;
        Integer key = new SecureRandom().nextInt(Integer.MAX_VALUE);
        Flux<SenderRecord<Integer, MessageDTO, Integer>> outboundFlux =
                Flux.just(SenderRecord.create(new ProducerRecord<>(AppConstants.HELLO_TOPIC, key, toSend), key));
        return this.sender
                .send(outboundFlux)
                .next()
                .flatMap(r -> {
                    if (r.exception() != null) {
                        return Mono.error(r.exception());
                    }
                    RecordMetadata metadata = r.recordMetadata();
                    log.info(
                            "Message {} sent successfully, topic-partition={}-{} offset={} timestamp={}",
                            r.correlationMetadata(),
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset(),
                            LocalDateTime.now());
                    return Mono.<Void>empty();
                })
                .timeout(this.sendTimeout)
                .doOnError(e -> log.error("Send failed", e));
    }
}
