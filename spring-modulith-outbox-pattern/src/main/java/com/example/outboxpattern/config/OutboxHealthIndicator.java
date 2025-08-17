package com.example.outboxpattern.config;

import java.time.Duration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.stereotype.Component;

@Component
public class OutboxHealthIndicator implements HealthIndicator {

    private final EventPublicationRegistry registry;

    public OutboxHealthIndicator(EventPublicationRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Health health() {
        long incompletePublications = registry.findIncompletePublications().size();

        long failedPublications = registry.findIncompletePublicationsOlderThan(Duration.ofMinutes(10))
                .size();
        Health.Builder builder = Health.up()
                .withDetail("pendingPublications", incompletePublications)
                .withDetail("failedPublications", failedPublications);

        // If there are too many failed publications, mark the health as DOWN
        if (failedPublications > 50) {
            return builder.down()
                    .withDetail("error", "Too many failed publications")
                    .build();
        }

        return builder.build();
    }
}
