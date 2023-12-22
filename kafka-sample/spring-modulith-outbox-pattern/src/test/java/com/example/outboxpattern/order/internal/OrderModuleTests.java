package com.example.outboxpattern.order.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.example.outboxpattern.config.ApplicationProperties;
import com.example.outboxpattern.order.OrderResponse;
import com.example.outboxpattern.order.internal.request.OrderRequest;
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

    @MockBean
    ApplicationProperties applicationProperties;

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

        given(applicationProperties.getOrderCreatedKafkaTopic()).willReturn("order-created");

        scenario.stimulate(() -> orders.saveOrder(new OrderRequest("Coffee")))
                .andWaitForEventOfType(OrderResponse.class)
                .toArriveAndVerify(event -> assertThat(event.product()).isEqualTo("Coffee"));
    }
}
