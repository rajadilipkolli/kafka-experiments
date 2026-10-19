package com.example.outboxpattern.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.stereotype.Component;

/**
 * Startup component that resubmits incomplete outbox publications with bounded batch,
 * in-flight, and age limits once the application is ready.
 */
@Component
public class OutboxEventResubmissionStartup {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventResubmissionStartup.class);

    private final IncompleteEventPublications incompleteEventPublications;
    private final ApplicationProperties applicationProperties;

    public OutboxEventResubmissionStartup(
            IncompleteEventPublications incompleteEventPublications, ApplicationProperties applicationProperties) {
        this.incompleteEventPublications = incompleteEventPublications;
        this.applicationProperties = applicationProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info(
                "Resubmitting incomplete event publications with batchSize={}, maxInFlight={}, minAge={}",
                applicationProperties.resubmissionBatchSize(),
                applicationProperties.resubmissionMaxInFlight(),
                applicationProperties.resubmissionMinAge());

        ResubmissionOptions options = ResubmissionOptions.defaults()
                .withBatchSize(applicationProperties.resubmissionBatchSize())
                .withMaxInFlight(applicationProperties.resubmissionMaxInFlight())
                .withMinAge(applicationProperties.resubmissionMinAge());

        incompleteEventPublications.resubmitIncompletePublications(options);
    }
}
