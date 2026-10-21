package com.example.outboxpattern.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class OutboxHealthIndicator implements HealthIndicator {

    private final OutboxStatsService statsService;

    /** Creates a health indicator backed by the supplied outbox statistics service. */
    public OutboxHealthIndicator(OutboxStatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * Reports {@code DOWN} when more than 50 unresolved failed records exist, regardless of age,
     * and {@code UP} otherwise. Statistics retrieval failures propagate to the caller.
     *
     * @return health with {@code pendingPublications} and {@code failedPublications} counts
     */
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
