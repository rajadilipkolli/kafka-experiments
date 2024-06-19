package com.example.outboxpattern.order.internal.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank(message = "Product cannot be blank") String productCode,
        BigDecimal productPrice,
        @Positive(message = "quantity cant be 0 or negative") Integer quantity) {}
