package com.example.outboxpattern.common.listener;

import com.example.outboxpattern.order.OrderRecord;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.annotation.KafkaListener;

@TestConfiguration
public class OrderListener {

    private static final Logger log = LoggerFactory.getLogger(OrderListener.class);
    private final CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = "order-created", groupId = "notification")
    public void notify(OrderRecord event) {
        log.info(
                "Notifying user for created order {} and productCode {}",
                event.id(),
                event.orderItems().getFirst().productCode());
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
