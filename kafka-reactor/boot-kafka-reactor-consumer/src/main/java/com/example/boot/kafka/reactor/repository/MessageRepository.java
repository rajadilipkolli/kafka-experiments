package com.example.boot.kafka.reactor.repository;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MessageRepository extends R2dbcRepository<MessageDTO, Long> {
    /**
     * Checks reactively whether a message with the event ID has already been persisted.
     *
     * @param eventId the event identifier to look up
     * @return a publisher emitting whether the event exists
     */
    Mono<Boolean> existsByEventId(UUID eventId);
}
