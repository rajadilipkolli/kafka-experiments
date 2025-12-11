package com.example.springbootkafkasample.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record KafkaListenerRequest(
        @NotBlank(message = "Container ID must not be blank") @Pattern(
                regexp = "^[a-zA-Z0-9\\-_.]+$",
                message = "Container ID must contain only alphanumeric characters, hyphens, underscores, and dots")
        @Pattern(regexp = "^\\S.*$", message = "Container ID must not start with whitespace") String containerId,

        @NotNull(message = "Operation must not be null") Operation operation) {}
