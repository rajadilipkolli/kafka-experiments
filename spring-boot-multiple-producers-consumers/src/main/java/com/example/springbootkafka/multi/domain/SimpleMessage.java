package com.example.springbootkafka.multi.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SimpleMessage(
                @Positive Integer key,
                @NotBlank String value) {
}
