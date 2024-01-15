package com.example.outboxpattern.order.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.outboxpattern.order.OrderRecord;
import com.example.outboxpattern.order.internal.request.OrderItemRequest;
import com.example.outboxpattern.order.internal.request.OrderRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

@Slf4j
@ApplicationModuleTest
class OrderModuleTests {

    @MockBean
    KafkaOperations<?, ?> kafkaOperations;

    @Autowired
    OrderService orders;

    @Test
    void shouldTriggerOrderCreatedEvent(Scenario scenario) {

        when(kafkaOperations.send(any(), any(), any())).then(invocation -> {
            log.info(
                    "Sending message key {}, value {} to {}.",
                    invocation.getArguments()[1],
                    invocation.getArguments()[2],
                    invocation.getArguments()[0]);
            return CompletableFuture.completedFuture(new SendResult<>(null, null));
        });

        scenario.stimulate(() -> orders.saveOrder(
                        new OrderRequest(List.of(new OrderItemRequest("Coffee", BigDecimal.TEN, 100)))))
                .andWaitForEventOfType(OrderRecord.class)
                .toArriveAndVerify(event ->
                        assertThat(event.orderItems().getFirst().productCode()).isEqualTo("Coffee"));
    }
}
