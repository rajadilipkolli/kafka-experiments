package com.example.boot.kafka.reactor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record MessageDTO(
        @JsonIgnore Long id, @NotBlank(message = "Text Value Cant be Blank") String text, LocalDateTime sentAt) {}
