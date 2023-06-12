package com.example.springbootkafkasample.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageDTO(
        @NotBlank(message = "Topic cant be Blank") String topic,
        @NotBlank(message = "Message cant be Blank") String msg) {}
