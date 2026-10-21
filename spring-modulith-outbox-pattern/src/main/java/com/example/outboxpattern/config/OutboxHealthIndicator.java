package com.example.outboxpattern.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class OutboxHealthIndicator implements HealthIndicator {

    private final OutboxStatsService statsService;

    public OutboxHealthIndicator(OutboxStatsService statsService) {
        this.statsService = statsService;
    }

    @Override
    public Health health() {
        OutboxStats stats = statsService.getStats();

        Health.Builder builder = Health.up()
                .withDetail("pendingPublications", stats.pendingCount())
                .withDetail("failedPublications", stats.failedCount());

        if (stats.failedCount() > 50) {
            return builder.down()
                    .withDetail("error", "Too many failed publications")
                    .build();
        }

        return builder.build();
    }
}
