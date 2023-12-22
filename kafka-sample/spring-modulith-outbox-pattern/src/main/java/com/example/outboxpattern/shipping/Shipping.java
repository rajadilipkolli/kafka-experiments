package com.example.outboxpattern.shipping;

import com.example.outboxpattern.order.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class Shipping {

    @ApplicationModuleListener
    void on(OrderResponse event) {
        ship(event.id());
    }

    private void ship(Long orderId) {
        log.info("Started shipping for order {}", orderId);
    }
}
