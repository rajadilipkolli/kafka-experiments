package com.github.timtebeek.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record OrderDto(@NotNull UUID orderId, @NotNull UUID articleId, @Positive int amount) {}
