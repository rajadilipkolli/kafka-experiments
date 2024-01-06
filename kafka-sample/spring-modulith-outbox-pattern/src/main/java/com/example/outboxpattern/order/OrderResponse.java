package com.example.outboxpattern.order;

import org.jmolecules.event.types.DomainEvent;
import org.springframework.modulith.events.Externalized;

@Externalized("order-created::#{id()}")
public record OrderResponse(Long id, String product, String status) implements DomainEvent {}
