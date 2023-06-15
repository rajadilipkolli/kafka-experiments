package com.example.boot.kafka.reactor.dto;

import java.time.LocalDateTime;
import org.springframework.data.relational.core.mapping.Table;

@Table
public record MessageDTO(Long id, String text, LocalDateTime sentAt) {}
