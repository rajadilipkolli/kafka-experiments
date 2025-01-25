package com.example.outboxpattern.order.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.outboxpattern.common.ContainersConfig;
import com.example.outboxpattern.common.SQLContainerConfig;
import com.example.outboxpattern.order.OrderRecord;
import com.example.outboxpattern.order.internal.domain.request.OrderItemRequest;
import com.example.outboxpattern.order.internal.domain.request.OrderRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ApplicationModuleTest
@Import({ContainersConfig.class, SQLContainerConfig.class})
class OrderModuleIntTests {

    private static final Logger log = LoggerFactory.getLogger(OrderModuleIntTests.class);

    @MockitoBean
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
                        new OrderRequest(null, List.of(new OrderItemRequest("Coffee", BigDecimal.TEN, 100)))))
                .andWaitForEventOfType(OrderRecord.class)
                .toArriveAndVerify(event ->
                        assertThat(event.orderItems().getFirst().productCode()).isEqualTo("Coffee"));
    }

    @Test
    void shouldCreateOrderWithMultipleItems(Scenario scenario) {
        when(kafkaOperations.send(any(), any(), any())).then(invocation -> {
            log.info(
                    "Sending message key {}, value {} to {}.",
                    invocation.getArguments()[1],
                    invocation.getArguments()[2],
                    invocation.getArguments()[0]);
            return CompletableFuture.completedFuture(new SendResult<>(null, null));
        });

        scenario.stimulate(() -> orders.saveOrder(new OrderRequest(
                        null,
                        List.of(
                                new OrderItemRequest("Coffee", BigDecimal.TEN, 100),
                                new OrderItemRequest("Tea", BigDecimal.valueOf(5), 50)))))
                .andWaitForEventOfType(OrderRecord.class)
                .toArriveAndVerify(event -> {
                    assertThat(event.orderItems().get(0).productCode()).isEqualTo("Coffee");
                    assertThat(event.orderItems().get(1).productCode()).isEqualTo("Tea");
                    assertThat(event.orderItems().get(0).quantity()).isEqualTo(100);
                    assertThat(event.orderItems().get(1).quantity()).isEqualTo(50);
                    assertThat(event.orderItems().get(0).productPrice()).isEqualTo(BigDecimal.TEN);
                    assertThat(event.orderItems().get(1).productPrice()).isEqualTo(BigDecimal.valueOf(5));
                });
    }
}
