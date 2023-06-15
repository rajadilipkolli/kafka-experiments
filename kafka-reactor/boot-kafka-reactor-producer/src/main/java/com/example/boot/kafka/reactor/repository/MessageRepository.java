package com.example.boot.kafka.reactor.repository;

import com.example.boot.kafka.reactor.dto.MessageDTO;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface MessageRepository extends R2dbcRepository<MessageDTO, UUID> {}
