package com.example.outboxpattern.order.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.outboxpattern.common.AbstractIntegrationTest;
import com.example.outboxpattern.common.listener.OrderListener;
import com.example.outboxpattern.order.internal.domain.request.OrderItemRequest;
import com.example.outboxpattern.order.internal.domain.request.OrderRequest;
import com.example.outboxpattern.order.internal.entities.Order;
import com.example.outboxpattern.order.internal.entities.OrderItem;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderListener orderListener;

    private List<Order> orderList = null;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAllInBatch();

        orderList = new ArrayList<>();
        orderList.add(new Order()
                .setOrderedDate(LocalDateTime.now())
                .setStatus(Order.OrderStatus.CREATED)
                .addOrderItem(new OrderItem()
                        .setProductCode("First Order")
                        .setProductPrice(BigDecimal.TWO)
                        .setQuantity(10)));
        orderList.add(new Order()
                .setOrderedDate(LocalDateTime.now().plusDays(1))
                .setStatus(Order.OrderStatus.CREATED)
                .addOrderItem(new OrderItem()
                        .setProductCode("Second Order")
                        .setProductPrice(BigDecimal.TEN)
                        .setQuantity(10)));
        orderList.add(new Order()
                .setOrderedDate(LocalDateTime.now().plusDays(2))
                .setStatus(Order.OrderStatus.CREATED)
                .addOrderItem(new OrderItem()
                        .setProductCode("Third Order")
                        .setProductPrice(BigDecimal.ONE)
                        .setQuantity(10)));
        orderList = orderRepository.saveAll(orderList);
    }

    @Test
    void shouldFetchAllOrders() throws Exception {
        this.mockMvc
                .perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.data.size()", is(orderList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Nested
    @DisplayName("find methods")
    class Find {
        @Test
        void shouldFindOrderById() throws Exception {
            Order order = orderList.getFirst();
            Long orderId = order.getId();

            mockMvc.perform(get("/api/orders/{id}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$.id", is(order.getId()), Long.class))
                    .andExpect(jsonPath("$.status", is("CREATED")))
                    .andExpect(jsonPath(
                            "$.orderItems[0].productCode",
                            is(order.getItems().getFirst().getProductCode())))
                    .andExpect(jsonPath("$.orderItems[0].productPrice", is(2.0)))
                    .andExpect(jsonPath(
                            "$.orderItems[0].quantity",
                            is(order.getItems().getFirst().getQuantity())));
        }

        @Test
        void shouldReturn404WhenFetchingNonExistingOrder() throws Exception {
            Long orderId = 10_000L;

            mockMvc.perform(get("/api/orders/{id}", orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.type", is("http://api.spring-modulith-outbox-pattern.com/errors/not-found")))
                    .andExpect(jsonPath("$.title", is("Order Not Found")))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.detail").value("Order with Id '%d' Not found".formatted(orderId)))
                    .andExpect(jsonPath("$.instance").value("/api/orders/10000"))
                    .andExpect(jsonPath("$.errorCategory").value("Generic"));
        }
    }

    @Nested
    @DisplayName("save methods")
    class Save {
        @Test
        void shouldCreateNewOrder() throws Exception {
            OrderRequest orderRequest =
                    new OrderRequest(null, List.of(new OrderItemRequest("New Order", BigDecimal.TEN, 100)));
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath(
                            "$.orderItems[0].productCode",
                            is(orderRequest.itemsList().getFirst().productCode())));

            long count = orderListener.getDlqLatch().getCount();
            await().pollInterval(Duration.ofSeconds(1))
                    .atMost(Duration.ofSeconds(15))
                    .untilAsserted(() -> {
                        assertThat(orderListener.getLatch().getCount()).isZero();
                        assertThat(orderListener.getDlqLatch().getCount()).isEqualTo(count);
                    });
        }

        @Test
        void shouldCreateNewOrderWithFailedStatus() throws Exception {
            long count = orderListener.getLatch().getCount();
            OrderRequest orderRequest = new OrderRequest(
                    Order.OrderStatus.FAILED.name(), List.of(new OrderItemRequest("New Order", BigDecimal.TEN, 100)));
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath(
                            "$.orderItems[0].productCode",
                            is(orderRequest.itemsList().getFirst().productCode())));

            await().pollInterval(Duration.ofSeconds(1))
                    .atMost(Duration.ofSeconds(15))
                    .untilAsserted(() -> {
                        assertThat(orderListener.getLatch().getCount()).isEqualTo(count);
                        assertThat(orderListener.getDlqLatch().getCount()).isZero();
                    });
        }

        @Test
        void shouldReturn400WhenCreateNewOrderWithoutItems() throws Exception {
            OrderRequest orderRequest = new OrderRequest(null, null);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.type", is("about:blank")))
                    .andExpect(jsonPath("$.title", is("Constraint Violation")))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                    .andExpect(jsonPath("$.instance", is("/api/orders")))
                    .andExpect(jsonPath("$.violations", hasSize(1)))
                    .andExpect(jsonPath("$.violations[0].field", is("itemsList")))
                    .andExpect(jsonPath("$.violations[0].message", is("ItemsList must not be empty")))
                    .andReturn();
        }
    }

    @Nested
    @DisplayName("update methods")
    class Update {
        @Test
        void shouldUpdateOrder() throws Exception {
            Long orderId = orderList.getFirst().getId();
            OrderRequest orderRequest = new OrderRequest(
                    null,
                    List.of(new OrderItemRequest(
                            orderList.getFirst().getItems().getFirst().getProductCode(), BigDecimal.TEN, 100)));

            mockMvc.perform(put("/api/orders/{id}", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequest)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$.id", is(orderId), Long.class))
                    .andExpect(jsonPath("$.status", is("COMPLETED")))
                    .andExpect(jsonPath("$.orderItems[0].productCode", is("First Order")))
                    .andExpect(jsonPath("$.orderItems[0].productPrice", is(10)))
                    .andExpect(jsonPath("$.orderItems[0].quantity", is(100)));
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistingOrder() throws Exception {
            Long orderId = 10_000L;
            OrderRequest order = new OrderRequest(null, List.of(new OrderItemRequest("Product1", BigDecimal.TEN, 10)));

            mockMvc.perform(put("/api/orders/{id}", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order)))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.type", is("http://api.spring-modulith-outbox-pattern.com/errors/not-found")))
                    .andExpect(jsonPath("$.title", is("Order Not Found")))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.detail").value("Order with Id '%d' Not found".formatted(orderId)))
                    .andExpect(jsonPath("$.instance").value("/api/orders/10000"))
                    .andExpect(jsonPath("$.errorCategory").value("Generic"));
        }
    }

    @Nested
    @DisplayName("delete methods")
    class Delete {
        @Test
        void shouldDeleteOrder() throws Exception {
            Order order = orderList.getFirst();

            mockMvc.perform(delete("/api/orders/{id}", order.getId()))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$.id", is(order.getId()), Long.class))
                    .andExpect(jsonPath(
                            "$.orderItems[0].productCode",
                            is(order.getItems().getFirst().getProductCode())));
        }

        @Test
        void shouldReturn404WhenDeletingNonExistingOrder() throws Exception {
            Long orderId = 1L;

            mockMvc.perform(delete("/api/orders/{id}", orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.type", is("http://api.spring-modulith-outbox-pattern.com/errors/not-found")))
                    .andExpect(jsonPath("$.title", is("Order Not Found")))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.detail").value("Order with Id '%d' Not found".formatted(orderId)))
                    .andExpect(jsonPath("$.instance").value("/api/orders/1"))
                    .andExpect(jsonPath("$.errorCategory").value("Generic"));
        }
    }
}
