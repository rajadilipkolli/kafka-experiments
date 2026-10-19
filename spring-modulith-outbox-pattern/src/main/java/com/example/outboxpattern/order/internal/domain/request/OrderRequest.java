package com.example.outboxpattern.order.internal.domain.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderRequest(
        String status,
        @NotEmpty(message = "ItemsList must not be empty") @Size(max = 100, message = "ItemsList must not exceed 100 items") List<OrderItemRequest> itemsList) {}
