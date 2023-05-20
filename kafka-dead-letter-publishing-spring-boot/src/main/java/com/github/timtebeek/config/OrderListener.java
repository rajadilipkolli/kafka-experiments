package com.github.timtebeek.config;

import com.github.timtebeek.dto.OrderDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
class OrderListener {

    private static final Logger log = LoggerFactory.getLogger(OrderListener.class);

    @KafkaListener(topics = KafkaConfiguration.ORDERS)
    void listen(@Payload @Validated OrderDto order) {
        log.info("Received: {}", order);
    }
}
