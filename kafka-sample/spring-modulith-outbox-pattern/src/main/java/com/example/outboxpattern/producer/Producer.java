package com.example.outboxpattern.producer;

import com.example.outboxpattern.order.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class Producer {

    @ApplicationModuleListener
    void onOrderResponseEvent(OrderResponse event) {
        publish(event.id());
    }

    private void publish(Long orderId) {
        log.info("Started publishing for order {}", orderId);
    }
}
