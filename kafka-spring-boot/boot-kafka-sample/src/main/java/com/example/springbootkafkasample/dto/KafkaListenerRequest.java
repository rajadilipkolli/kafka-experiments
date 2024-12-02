package com.example.springbootkafkasample.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KafkaListenerRequest(
        @NotBlank(message = "Container ID must not be blank") String containerId,
        @NotNull(message = "Operation must not be null") Operation operation) {}
