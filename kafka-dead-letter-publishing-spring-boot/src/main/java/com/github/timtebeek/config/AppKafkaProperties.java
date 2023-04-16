package com.github.timtebeek.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.kafka")
@Validated
public record AppKafkaProperties(
        @NotNull @Valid DeadLetter deadletter, @NotNull @Valid Backoff backoff) {}

record DeadLetter(@NotNull Duration retention, @Nullable String suffix) {}

record Backoff(
        @NotNull Duration initialInterval,
        @NotNull Duration maxInterval,
        @Positive int maxRetries,
        @Positive double multiplier) {}
