package com.example.boot.kafka.reactor.repository;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface MessageRepository extends R2dbcRepository<MessageDTO, Long> {}
