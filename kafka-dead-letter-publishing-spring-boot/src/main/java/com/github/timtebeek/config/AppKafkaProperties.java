package com.github.timtebeek.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.kafka")
@Validated
public record AppKafkaProperties(
        @NotNull @Valid DeadLetter deadletter, @NotNull @Valid Backoff backoff) {}
