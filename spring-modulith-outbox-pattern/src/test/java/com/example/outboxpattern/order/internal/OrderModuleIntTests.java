package com.example.outboxpattern.order.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.outboxpattern.common.SQLContainerConfig;
import com.example.outboxpattern.common.TestDataHelper;
import com.example.outboxpattern.order.OrderRecord;
import com.example.outboxpattern.order.internal.domain.request.OrderItemRequest;
import com.example.outboxpattern.order.internal.domain.request.OrderRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestPropertySource;

@ApplicationModuleTest(classes = {SQLContainerConfig.class})
@Import(TestDataHelper.class)
@TestPropertySource(properties = "spring.modulith.events.externalization.enabled=false")
class OrderModuleIntTests {

    @Autowired
    OrderService orders;

    @Autowired
    TestDataHelper testDataHelper;

    @Test
    void shouldTriggerOrderCreatedEvent(Scenario scenario) {
        scenario.stimulate(() -> orders.saveOrder(
                        new OrderRequest(null, List.of(new OrderItemRequest("Coffee", BigDecimal.TEN, 100)))))
                .andWaitForEventOfType(OrderRecord.class)
                .toArriveAndVerify(event ->
                        assertThat(event.orderItems().getFirst().productCode()).isEqualTo("Coffee"));
    }

    @Test
    void shouldCreateOrderWithMultipleItems(Scenario scenario) {
        scenario.stimulate(() -> orders.saveOrder(new OrderRequest(
                        null,
                        List.of(
                                new OrderItemRequest("Coffee", BigDecimal.TEN, 100),
                                new OrderItemRequest("Tea", BigDecimal.valueOf(5), 50)))))
                .andWaitForEventOfType(OrderRecord.class)
                .toArriveAndVerify(event -> {
                    assertThat(event.orderItems()).hasSize(2);
                    var coffeeItem = event.orderItems().stream()
                            .filter(item -> item.productCode().equals("Coffee"))
                            .findFirst()
                            .orElseThrow();
                    var teaItem = event.orderItems().stream()
                            .filter(item -> item.productCode().equals("Tea"))
                            .findFirst()
                            .orElseThrow();
                    assertThat(coffeeItem.productCode()).isEqualTo("Coffee");
                    assertThat(coffeeItem.quantity()).isEqualTo(100);
                    assertThat(coffeeItem.productPrice()).isEqualTo(BigDecimal.TEN);
                    assertThat(teaItem.productCode()).isEqualTo("Tea");
                    assertThat(teaItem.quantity()).isEqualTo(50);
                    assertThat(teaItem.productPrice()).isEqualTo(BigDecimal.valueOf(5));
                });
    }

    @Test
    void shouldUpdateExistingOrder() {
        Long orderId = testDataHelper.insertOrder(LocalDateTime.now(), "CREATED");
        testDataHelper.insertOrderItem(orderId, "Product", BigDecimal.TEN, 1);

        OrderRequest request = new OrderRequest(null, List.of(new OrderItemRequest("Product", BigDecimal.ONE, 2)));

        orders.updateOrder(orderId, request);

        var orderMap = testDataHelper.findOrderById(orderId);
        org.assertj.core.api.Assertions.assertThat(orderMap.get("status")).isEqualTo("COMPLETED");
    }

    @Test
    void shouldDeleteOrder() {
        Long orderId = testDataHelper.insertOrder(LocalDateTime.now(), "CREATED");
        testDataHelper.insertOrderItem(orderId, "Product", BigDecimal.TEN, 1);

        orders.deleteOrderById(orderId);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> testDataHelper.findOrderById(orderId))
                .isInstanceOf(org.springframework.dao.EmptyResultDataAccessException.class);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound(Scenario scenario) {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> orders.findOrderById(100_000L).orElseThrow(() -> new OrderNotFoundException(100_000L)))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentOrder(Scenario scenario) {
        OrderRequest request = new OrderRequest(null, List.of(new OrderItemRequest("Product", BigDecimal.TEN, 1)));
        assertThatThrownBy(() -> orders.updateOrder(100_000L, request)).isInstanceOf(OrderNotFoundException.class);
    }
}
