package com.example.outboxpattern.common.listener;

import com.example.outboxpattern.order.OrderRecord;
import com.example.outboxpattern.order.internal.entities.Order;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

@TestConfiguration(proxyBeanMethods = false)
public class OrderListener {

    private static final Logger log = LoggerFactory.getLogger(OrderListener.class);

    private final CountDownLatch latch = new CountDownLatch(1);
    private final CountDownLatch dlqLatch = new CountDownLatch(1);

    @RetryableTopic(
            attempts = "2",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = "order-created", groupId = "notification")
    public void notify(OrderRecord event) {
        log.info(
                "Notifying user for created order {} and productCode {}",
                event.id(),
                event.orderItems().getFirst().productCode());
        if (event.status().equals(Order.OrderStatus.FAILED.name())) {
            throw new RuntimeException("Simulating failure for order:" + event.id());
        }
        latch.countDown();
    }

    @DltHandler
    public void notifyDLT(OrderRecord event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error(
                "Order processing failed, received in DLT - OrderId: {}, Status: {}, Items: {} from topic: {}",
                event.id(),
                event.status(),
                event.orderItems(),
                topic);
        dlqLatch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public CountDownLatch getDlqLatch() {
        return dlqLatch;
    }
}
