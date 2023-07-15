package com.example.boot.kafka.reactor.service;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.repository.MessageRepository;
import com.example.boot.kafka.reactor.util.AppConstants;
import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final KafkaSender<Integer, MessageDTO> sender;
    private final MessageRepository messageRepository;

    public Mono<ResponseEntity<Object>> sendMessage(MessageDTO messageDTO) {
        Integer key = new SecureRandom().nextInt(Integer.MAX_VALUE);
        return saveMessage(messageDTO)
                .doOnSuccess(it -> this.sender
                        .send(Flux.just(
                                SenderRecord.create(new ProducerRecord<>(AppConstants.HELLO_TOPIC, key, it), key)))
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
                        }))
                .map(it -> ResponseEntity.created(URI.create("/messages/" + it.id()))
                        .build());
    }

    private Mono<MessageDTO> saveMessage(MessageDTO messageDTO) {
        return messageRepository.save(messageDTO);
    }
}
