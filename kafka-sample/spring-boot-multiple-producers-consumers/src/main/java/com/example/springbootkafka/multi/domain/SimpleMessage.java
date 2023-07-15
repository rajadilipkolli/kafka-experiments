package com.example.springbootkafka.multi.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SimpleMessage(
        @Positive(message = "Key should be positive") Integer key,
        @NotBlank(message = "Value shouldn't be Blank") String value) {}
