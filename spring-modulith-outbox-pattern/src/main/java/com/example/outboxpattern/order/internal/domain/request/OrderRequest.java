package com.example.outboxpattern.order.internal.domain.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderRequest(
        String status, @NotEmpty(message = "ItemsList must not be empty") List<OrderItemRequest> itemsList) {}
