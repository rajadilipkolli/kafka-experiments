package com.example.outboxpattern.common.listener;

import com.example.outboxpattern.order.OrderRecord;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@TestConfiguration
@Getter
public class OrderListener {

    private final CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = "order-created", groupId = "notification")
    public void notify(OrderRecord event) {
        log.info(
                "Notifying user for created order {} and productCode {}",
                event.id(),
                event.orderItems().getFirst().productCode());
        latch.countDown();
    }
}
