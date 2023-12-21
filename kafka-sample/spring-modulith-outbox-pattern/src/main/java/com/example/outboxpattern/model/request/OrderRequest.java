package com.example.outboxpattern.model.request;

import jakarta.validation.constraints.NotEmpty;

public record OrderRequest(@NotEmpty(message = "Text cannot be empty") String text) {}
