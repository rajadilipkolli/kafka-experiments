package com.example.outboxpattern.order;

import java.time.LocalDateTime;
import java.util.List;
import org.jmolecules.event.types.DomainEvent;
import org.springframework.modulith.events.Externalized;

@Externalized("order-created::#{id()}")
public record OrderRecord(Long id, LocalDateTime orderedDate, String status, List<OrderItemRecord> orderItems)
        implements DomainEvent {}
