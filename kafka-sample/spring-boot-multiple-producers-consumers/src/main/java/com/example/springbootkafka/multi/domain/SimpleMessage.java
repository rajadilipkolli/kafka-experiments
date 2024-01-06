package com.example.springbootkafka.multi.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SimpleMessage(
        @Positive(message = "Id should be positive") Integer id,
        @NotBlank(message = "Value shouldn't be Blank") String value) {}
