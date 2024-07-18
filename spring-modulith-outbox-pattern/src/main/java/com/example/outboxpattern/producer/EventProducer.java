package com.example.outboxpattern.producer;

import com.example.outboxpattern.order.OrderRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
class EventProducer {

    private static final Logger log = LoggerFactory.getLogger(EventProducer.class);

    @ApplicationModuleListener
    void onOrderResponseEvent(OrderRecord orderRecord) {
        log.info("Received Event :{}", orderRecord);
        publish(orderRecord.id());
    }

    private void publish(Long orderId) {
        log.info("Started publishing for order {}", orderId);
    }
}
