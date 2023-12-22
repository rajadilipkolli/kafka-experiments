package com.example.outboxpattern.common.listener;

import com.example.outboxpattern.order.response.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@TestComponent
public class OrderListener {

    @KafkaListener(topics = "order-created", groupId = "notification")
    public void notify(OrderResponse event) {
        log.info("Notifying user for created order {} and product {}", event.id(), event.product());
    }
}
