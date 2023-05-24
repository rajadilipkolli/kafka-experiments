package com.github.timtebeek.config;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.lang.Nullable;

record DeadLetter(@NotNull Duration retention, @Nullable String suffix) {}
