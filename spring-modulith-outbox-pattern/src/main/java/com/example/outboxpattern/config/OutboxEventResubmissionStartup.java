package com.example.outboxpattern.config;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
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

    public OutboxEventResubmissionStartup(
            IncompleteEventPublications incompleteEventPublications,
            ApplicationProperties applicationProperties,
            EventPublicationRepository publicationRepository) {
        this.incompleteEventPublications = incompleteEventPublications;
        this.applicationProperties = applicationProperties;
        this.publicationRepository = publicationRepository;
    }

    /**
     * Requests resubmission with the configured batch, in-flight, and minimum-age limits on startup
     * and on the scheduled delay. Then selects stale published or processing publications, up to
     * the smaller of the batch size and remaining resubmitted-event headroom. Staleness requires a
     * publication date strictly before the minimum-age cutoff. Repository and resubmission failures
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

        incompleteEventPublications.resubmitIncompletePublications(options);

        // The options overload only selects FAILED publications. Recover stale publications
        // left PUBLISHED or PROCESSING as well, within the remaining in-flight headroom.
        long headroom = options.getMaxInFlight() - publicationRepository.countByStatus(Status.RESUBMITTED);
        if (headroom <= 0) {
            return;
        }
        AtomicInteger remaining = new AtomicInteger((int) Math.min(options.getBatchSize(), headroom));
        Instant cutoff = Instant.now().minus(options.getMinAge());
        incompleteEventPublications.resubmitIncompletePublications(publication ->
                (publication.getStatus() == Status.PUBLISHED || publication.getStatus() == Status.PROCESSING)
                        && publication.getPublicationDate().isBefore(cutoff)
                        && remaining.getAndDecrement() > 0);
    }
}
