package com.example.outboxpattern.order.internal.request;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank(message = "Product cannot be blank") String productCode, BigDecimal productPrice, int quantity) {}
