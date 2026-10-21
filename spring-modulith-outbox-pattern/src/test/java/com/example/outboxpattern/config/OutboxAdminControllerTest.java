package com.example.outboxpattern.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Unit tests for OutboxAdminController. */
@WebMvcTest(OutboxAdminController.class)
class OutboxAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OutboxStatsService statsService;

    @Test
    void shouldReturnStatsWithNoIncompletePublications() throws Exception {
        when(statsService.getStats()).thenReturn(new OutboxStats(0L, 0L));

        mockMvc.perform(get("/api/admin/outbox/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pendingCount").value(0))
                .andExpect(jsonPath("$.failedCount").value(0))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldReturnCorrectCountsWithMixedPublications() throws Exception {
        when(statsService.getStats()).thenReturn(new OutboxStats(5L, 3L));

        mockMvc.perform(get("/api/admin/outbox/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pendingCount").value(5))
                .andExpect(jsonPath("$.failedCount").value(3))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldHandleLargeNumberOfPublications() throws Exception {
        when(statsService.getStats()).thenReturn(new OutboxStats(1000L, 200L));

        mockMvc.perform(get("/api/admin/outbox/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pendingCount").value(1000))
                .andExpect(jsonPath("$.failedCount").value(200))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldHandleExceptionInStatsService() throws Exception {
        when(statsService.getStats()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/admin/outbox/stats")).andExpect(status().isInternalServerError());
    }
}
