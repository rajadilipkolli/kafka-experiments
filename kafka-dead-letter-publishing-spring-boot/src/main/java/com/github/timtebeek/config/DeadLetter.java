package com.github.timtebeek.config;

import jakarta.validation.constraints.NotNull;

import org.springframework.lang.Nullable;

import java.time.Duration;

record DeadLetter(@NotNull Duration retention, @Nullable String suffix) {}
