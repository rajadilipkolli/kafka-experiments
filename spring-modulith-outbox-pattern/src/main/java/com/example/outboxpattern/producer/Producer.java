package com.example.outboxpattern.producer;

import com.example.outboxpattern.order.OrderRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class Producer {

    @ApplicationModuleListener
    void onOrderResponseEvent(OrderRecord orderRecord) {
        log.info("Received Event :{}", orderRecord);
        publish(orderRecord.id());
    }

    private void publish(Long orderId) {
        log.info("Started publishing for order {}", orderId);
    }
}
