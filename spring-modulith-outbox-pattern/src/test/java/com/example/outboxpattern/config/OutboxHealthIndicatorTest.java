package com.example.outboxpattern.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

class OutboxHealthIndicatorTest {

    private final OutboxStatsService statsService = mock(OutboxStatsService.class);
    private final OutboxHealthIndicator indicator = new OutboxHealthIndicator(statsService);

    @Test
    void reportsHealthUpWhenFailuresWithinThreshold() {
        when(statsService.getStats()).thenReturn(new OutboxStats(10L, 5L));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("pendingPublications", 10L);
        assertThat(health.getDetails()).containsEntry("failedPublications", 5L);
    }

    @Test
    void reportsHealthDownWhenFailuresExceedThreshold() {
        when(statsService.getStats()).thenReturn(new OutboxStats(10L, 51L));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "Too many failed publications");
    }
}
