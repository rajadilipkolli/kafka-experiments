package com.example.boot.kafka.reactor.repository;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MessageRepository extends R2dbcRepository<MessageDTO, Long> {
    Mono<Boolean> existsByEventId(UUID eventId);
}
