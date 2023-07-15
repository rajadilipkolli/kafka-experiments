package com.example.boot.kafka.reactor.service;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.repository.MessageRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final KafkaReceiver<Integer, MessageDTO> receiver;
    private final MessageRepository messageRepository;

    private Mono<MessageDTO> saveMessage(MessageDTO messageDTO) {
        return messageRepository.save(messageDTO);
    }

    public Flux<MessageDTO> fetchMessages() {
        return receiver.receive()
                .map(record -> {
                    ReceiverOffset offset = record.receiverOffset();
                    var value = record.value();
                    log.info(
                            "Received message: topic-partition={} offset={} timestamp={} key={} value={}",
                            offset.topicPartition(),
                            offset.offset(),
                            LocalDateTime.now(),
                            record.key(),
                            value);
                    offset.acknowledge();
                    return value;
                })
                .doOnNext(messageDTO -> saveMessage(messageDTO));
    }
}
