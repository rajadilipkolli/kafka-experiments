package com.example.outboxpattern.order.internal;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderRepository extends JpaRepository<Order, Long> {}
