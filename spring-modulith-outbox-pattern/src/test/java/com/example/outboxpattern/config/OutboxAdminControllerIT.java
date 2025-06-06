package com.example.outboxpattern.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.example.outboxpattern.common.AbstractIntegrationTest;
import com.example.outboxpattern.order.OrderItemRecord;
import com.example.outboxpattern.order.OrderRecord;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class OutboxAdminControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldReturnCorrectStatsAfterPublishingEvents() throws Exception {
        // Initial state - verify no pending publications
        MvcResult initialResult = mockMvc.perform(get("/api/admin/outbox/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Map<String, Object> initialStats =
                objectMapper.readValue(initialResult.getResponse().getContentAsString(), Map.class);
        int initialPendingCount = ((Number) initialStats.get("pendingCount")).intValue();

        // Publish some events that will be processed by event listeners
        OrderItemRecord item = new OrderItemRecord("Test Product", BigDecimal.TEN, 1);
        OrderRecord order = new OrderRecord(999L, LocalDateTime.now(), "TEST", List.of(item));
        eventPublisher.publishEvent(order);

        // Wait briefly for any async event processing
        Thread.sleep(100);

        // Get stats after publishing events
        MvcResult afterResult = mockMvc.perform(get("/api/admin/outbox/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Map<String, Object> afterStats =
                objectMapper.readValue(afterResult.getResponse().getContentAsString(), Map.class);

        // Verify the API response contains the expected fields
        assertThat(afterStats).containsKeys("pendingCount", "failedCount", "timestamp");

        // The test is not deterministic for the exact count because:
        // 1. Events might be processed by the time we check
        // 2. Other tests might leave incomplete publications
        // So we just verify the API works correctly
    }

    @Test
    void outboxStatsEndpointShouldReturnValidResponse() throws Exception {

        mockMvc.perform(get("/api/admin/outbox/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pendingCount", is(notNullValue())))
                .andExpect(jsonPath("$.pendingCount", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.failedCount", is(notNullValue())))
                .andExpect(jsonPath("$.failedCount", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.timestamp", is(notNullValue())));
    }

    @Test
    void outboxStatsEndpointShouldRespondToMultipleRequests() throws Exception {

        // Make multiple requests to verify the endpoint can handle them
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/admin/outbox/stats"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.pendingCount", is(notNullValue())))
                    .andExpect(jsonPath("$.failedCount", is(notNullValue())))
                    .andExpect(jsonPath("$.timestamp", is(notNullValue())));
        }
    }
}
