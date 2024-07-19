package com.example.outboxpattern.common.listener;

import com.example.outboxpattern.order.OrderRecord;
import com.example.outboxpattern.order.internal.entities.Order;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@TestConfiguration
public class OrderListener {

    private static final Logger log = LoggerFactory.getLogger(OrderListener.class);

    private final CountDownLatch latch = new CountDownLatch(1);
    private final CountDownLatch dlqLatch = new CountDownLatch(1);

    /*
     * Boot will autowire this into the container factory.
     */
    @Bean
    CommonErrorHandler errorHandler(KafkaOperations<Object, Object> template) {
        return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(template), new FixedBackOff(1000L, 2));
    }

    @KafkaListener(topics = "order-created", groupId = "notification")
    public void notify(OrderRecord event) {
        log.info(
                "Notifying user for created order {} and productCode {}",
                event.id(),
                event.orderItems().getFirst().productCode());
        if (event.status().equals(Order.OrderStatus.FAILED.name())) {
            throw new RuntimeException("failed");
        }
        latch.countDown();
    }

    @KafkaListener(id = "dltGroup", topics = "order-created.DLT")
    public void dltListen(byte[] in) {
        log.info("Received from DLT: {}", new String(in));
        dlqLatch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public CountDownLatch getDlqLatch() {
        return dlqLatch;
    }
}
