package com.example.boot.kafka.reactor.entity;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "messages")
public record MessageDTO(
        @Id Long id, @NotBlank(message = "Text Value Cant be Blank") String text, LocalDateTime sentAt) {}
