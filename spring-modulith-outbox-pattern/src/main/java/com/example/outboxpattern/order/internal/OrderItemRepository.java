package com.example.outboxpattern.order.internal;

import com.example.outboxpattern.order.internal.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
