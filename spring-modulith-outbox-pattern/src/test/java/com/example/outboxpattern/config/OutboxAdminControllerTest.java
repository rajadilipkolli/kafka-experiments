package com.example.outboxpattern.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.modulith.events.core.PublicationTargetIdentifier;
import org.springframework.modulith.events.core.TargetEventPublication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for OutboxAdminController.
 */
@WebMvcTest(OutboxAdminController.class)
class OutboxAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventPublicationRegistry eventPublicationRegistry;
    /**
     * Mock implementation of EventPublication interface for testing.
     */
    static class MockEventPublication implements TargetEventPublication {
        private final UUID id;
        private final Instant publicationDate;
        private final Instant completionDate;
        private final String serializedEvent;

        public MockEventPublication(UUID id, Instant publicationDate, Instant completionDate) {
            this.id = Objects.requireNonNull(id);
            this.publicationDate = Objects.requireNonNull(publicationDate);
            this.completionDate = completionDate;
            this.serializedEvent = "{}"; // Simple JSON for tests
        }

        @Override
        public UUID getIdentifier() {
            return id;
        }

        @Override
        public Object getEvent() {
            return serializedEvent;
        }

        @Override
        public Instant getPublicationDate() {
            return publicationDate;
        }

        @Override
        public Optional<Instant> getCompletionDate() {
            return Optional.ofNullable(completionDate);
        }

        @Override
        public PublicationTargetIdentifier getTargetIdentifier() {
            return PublicationTargetIdentifier.of("dummy");
        }

        @Override
        public void markCompleted(Instant instant) {}

        @Override
        public int getCompletionAttempts() {
            return 0; // For test purposes, we return 0 completion attempts
        }

        @Override
        public Instant getLastResubmissionDate() {
            return Instant.now().minusSeconds(30); // For test purposes, we return a fixed instant
        }

        @Override
        public Status getStatus() {
            return Status.COMPLETED;
        }
    }

    @Test
    void shouldReturnStatsWithNoIncompletePublications() throws Exception {
        // Given - empty list of incomplete publications
        when(eventPublicationRegistry.findIncompletePublications()).thenReturn(Collections.emptyList());

        // When/Then
        mockMvc.perform(get("/api/admin/outbox/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pendingCount").value(0))
                .andExpect(jsonPath("$.failedCount").value(0))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldReturnCorrectCountsWithMixedPublications() throws Exception {
        // Given - list of publications with mixed timestamps
        List<TargetEventPublication> incompletePublications = new ArrayList<>();

        // Create 5 recent publications (pending)
        Instant now = Instant.now();
        for (int i = 0; i < 5; i++) {
            incompletePublications.add(new MockEventPublication(
                    UUID.randomUUID(),
                    now.minusSeconds(i * 60), // Recent publications
                    null));
        }

        // Create 3 older publications (failed)
        for (int i = 0; i < 3; i++) {
            incompletePublications.add(new MockEventPublication(
                    UUID.randomUUID(),
                    now.minus(2, ChronoUnit.DAYS), // Older than 1 day
                    null));
        }

        when(eventPublicationRegistry.findIncompletePublications()).thenReturn(incompletePublications);
        when(eventPublicationRegistry.findIncompletePublicationsOlderThan(any(Duration.class)))
                .thenReturn(incompletePublications.subList(5, 8)); // Return only the 3 older publications

        // When/Then
        mockMvc.perform(get("/api/admin/outbox/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pendingCount").value(8)) // Total incomplete
                .andExpect(jsonPath("$.failedCount").value(3)) // Older than 1 day
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldHandleLargeNumberOfPublications() throws Exception {
        // Given - generate a large list of incomplete publications
        int totalCount = 1000;
        int failedCount = 200;

        List<TargetEventPublication> incompletePublications = IntStream.range(0, totalCount)
                .mapToObj(i -> {
                    Instant publicationDate = i < failedCount
                            ? Instant.now().minus(2, ChronoUnit.DAYS) // Failed publications
                            : Instant.now().minusSeconds(i); // Recent publications

                    return new MockEventPublication(UUID.randomUUID(), publicationDate, null);
                })
                .collect(Collectors.toList());

        List<TargetEventPublication> failedPublications = incompletePublications.subList(0, failedCount);

        when(eventPublicationRegistry.findIncompletePublications()).thenReturn(incompletePublications);
        when(eventPublicationRegistry.findIncompletePublicationsOlderThan(any(Duration.class)))
                .thenReturn(failedPublications);

        // When/Then
        mockMvc.perform(get("/api/admin/outbox/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pendingCount").value(totalCount))
                .andExpect(jsonPath("$.failedCount").value(failedCount))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldHandleExceptionInFindIncompletePublications() throws Exception {
        // Given - simulate exception when finding incomplete publications
        doThrow(new RuntimeException("Database error"))
                .when(eventPublicationRegistry)
                .findIncompletePublications();

        // When/Then - controller should return 500 Internal Server Error
        mockMvc.perform(get("/api/admin/outbox/stats")).andExpect(status().isInternalServerError());
    }

    @Test
    void shouldHandleExceptionInFindIncompletePublicationsOlderThan() throws Exception {

        // Given - list of incomplete publications, but exception when filtering by age
        List<TargetEventPublication> incompletePublications =
                Collections.singletonList(new MockEventPublication(UUID.randomUUID(), Instant.now(), null));

        when(eventPublicationRegistry.findIncompletePublications()).thenReturn(incompletePublications);
        doThrow(new RuntimeException("Filtering error"))
                .when(eventPublicationRegistry)
                .findIncompletePublicationsOlderThan(any(Duration.class));

        // When/Then - controller should return 500 Internal Server Error
        mockMvc.perform(get("/api/admin/outbox/stats")).andExpect(status().isInternalServerError());
    }
}
