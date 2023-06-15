package com.example.boot.kafka.reactor.controller;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.boot.kafka.reactor.repository.MessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
class MessageController {
    private final KafkaSender<Integer, String> sender;
    private final MessageRepository messageRepository;

    @PostMapping
    public Mono<ResponseEntity<Object>> sendMessage(@RequestBody Message message) {
        log.debug("sending message: {}", message);
        Integer key = new SecureRandom().nextInt(Integer.MAX_VALUE);
        return messageRepository.save(message)
                .doOnSuccess(it -> {
                            var notification = "Message #" + it.id() + " was sent at " + it.sentAt();
                            this.sender.send(Flux.just(SenderRecord.create(new ProducerRecord<>(SenderApplication.HELLO_TOPIC, key, notification), key)))
                                    .doOnError(e -> log.error("Send failed", e))
                                    .subscribe(r -> {
                                        RecordMetadata metadata = r.recordMetadata();
                                        log.debug("Message {} sent successfully, topic-partition={}-{} offset={} timestamp={}",
                                                r.correlationMetadata(),
                                                metadata.topic(),
                                                metadata.partition(),
                                                metadata.offset(),
                                                LocalDateTime.now()
                                        );
                                    });
                        }
                )
                .map(it -> ResponseEntity.created(URI.create("/messages/" + it.id())).build());
    }
}

