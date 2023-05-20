package com.github.timtebeek.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Duration;

record Backoff(
        @NotNull Duration initialInterval,
        @NotNull Duration maxInterval,
        @Positive short maxRetries,
        @Positive double multiplier) {}
