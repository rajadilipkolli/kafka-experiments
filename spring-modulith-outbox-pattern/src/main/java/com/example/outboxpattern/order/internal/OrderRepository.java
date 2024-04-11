package com.example.outboxpattern.order.internal;

import com.example.outboxpattern.order.internal.entities.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select u from Order u left join fetch u.items where u.id = :id")
    Optional<Order> findOrderById(@Param("id") Long id);
}
