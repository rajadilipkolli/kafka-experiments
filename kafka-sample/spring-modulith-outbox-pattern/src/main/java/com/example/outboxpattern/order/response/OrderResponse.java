package com.example.outboxpattern.order.response;

import org.springframework.modulith.events.Externalized;

@Externalized("order-created::#{id()}")
public record OrderResponse(Long id, String product, String status) {}
