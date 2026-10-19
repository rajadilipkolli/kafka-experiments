package com.example.outboxpattern.config;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.EventPublication.Status;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.modulith.events.core.EventPublicationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Resubmits incomplete outbox publications on startup and periodically with bounded
 * batch, in-flight, and age limits.
 */
@Component
public class OutboxEventResubmissionStartup {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventResubmissionStartup.class);

    private final IncompleteEventPublications incompleteEventPublications;
    private final ApplicationProperties applicationProperties;
    private final EventPublicationRepository publicationRepository;
    private final OutboxEventPublicationRecovery publicationRecovery;

    public OutboxEventResubmissionStartup(
            IncompleteEventPublications incompleteEventPublications,
            ApplicationProperties applicationProperties,
            EventPublicationRepository publicationRepository,
            OutboxEventPublicationRecovery publicationRecovery) {
        this.incompleteEventPublications = incompleteEventPublications;
        this.applicationProperties = applicationProperties;
        this.publicationRepository = publicationRepository;
        this.publicationRecovery = publicationRecovery;
    }

    /**
     * Requests resubmission with the configured batch, in-flight, and minimum-age limits on startup
     * and on the scheduled delay. First recovers resubmitted publications whose last attempt (or
     * original publication date if absent) is strictly before the minimum-age cutoff. Then recovers
     * published or processing publications with a publication date strictly before the cutoff,
     * selecting at most the smaller of the batch size and remaining headroom in the database.
     * Repository and resubmission failures
     * propagate to the invoking event listener or scheduler.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(
            initialDelayString = "${application.resubmission-min-age:5m}",
            fixedDelayString = "${application.resubmission-min-age:5m}")
    public synchronized void onApplicationReady() {
        log.info(
                "Resubmitting incomplete event publications with batchSize={}, maxInFlight={}, minAge={}",
                applicationProperties.resubmissionBatchSize(),
                applicationProperties.resubmissionMaxInFlight(),
                applicationProperties.resubmissionMinAge());

        ResubmissionOptions options = ResubmissionOptions.defaults()
                .withBatchSize(applicationProperties.resubmissionBatchSize())
                .withMaxInFlight(applicationProperties.resubmissionMaxInFlight())
                .withMinAge(applicationProperties.resubmissionMinAge());

        Instant cutoff = Instant.now().minus(options.getMinAge());
        publicationRecovery.recoverStaleResubmissions(cutoff);
        incompleteEventPublications.resubmitIncompletePublications(options);

        // The options overload only selects FAILED publications. Recover stale publications
        // left PUBLISHED or PROCESSING as well, within the remaining in-flight headroom.
        long headroom = options.getMaxInFlight() - publicationRepository.countByStatus(Status.RESUBMITTED);
        if (headroom <= 0) {
            return;
        }
        int remaining = (int) Math.min(options.getBatchSize(), headroom);
        if (publicationRecovery.recoverStalePublications(cutoff, remaining) > 0) {
            incompleteEventPublications.resubmitIncompletePublications(options.withBatchSize(remaining));
        }
    }
}
