package com.example.outboxpattern.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.outboxpattern.ModulithApplication;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.EventPublication.Status;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.modulith.events.core.EventPublicationRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfiguration;

class OutboxEventResubmissionStartupTest {

    @Test
    void shouldResubmitWithConfiguredOptionsOnApplicationReady() {
        IncompleteEventPublications publications = mock(IncompleteEventPublications.class);
        ApplicationProperties properties = new ApplicationProperties(
                "order-created",
                new ApplicationProperties.Cors("/api/**", "*", "*", "*", true),
                50,
                100,
                75,
                25,
                Duration.ofMinutes(10));

        OutboxEventResubmissionStartup startup =
                new OutboxEventResubmissionStartup(publications, properties, mock(EventPublicationRepository.class));
        startup.onApplicationReady();

        ArgumentCaptor<ResubmissionOptions> captor = ArgumentCaptor.forClass(ResubmissionOptions.class);
        verify(publications).resubmitIncompletePublications(captor.capture());

        ResubmissionOptions captured = captor.getValue();
        assertThat(captured.getBatchSize()).isEqualTo(75);
        assertThat(captured.getMaxInFlight()).isEqualTo(25);
        assertThat(captured.getMinAge()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void recoverySelectsOnlyStalePublishedAndProcessingWithinRemainingHeadroom() {
        IncompleteEventPublications publications = mock(IncompleteEventPublications.class);
        EventPublicationRepository repository = mock(EventPublicationRepository.class);
        when(repository.countByStatus(Status.RESUBMITTED)).thenReturn(23);
        OutboxEventResubmissionStartup startup =
                new OutboxEventResubmissionStartup(publications, properties(), repository);
        startup.onApplicationReady();

        ArgumentCaptor<Predicate<EventPublication>> captor = ArgumentCaptor.captor();
        verify(publications).resubmitIncompletePublications(captor.capture());
        Predicate<EventPublication> filter = captor.getValue();
        assertThat(filter.test(publication(Status.PUBLISHED, Duration.ZERO))).isFalse();
        assertThat(filter.test(publication(Status.PROCESSING, Duration.ZERO))).isFalse();
        assertThat(filter.test(publication(Status.FAILED, Duration.ofHours(1)))).isFalse();
        assertThat(filter.test(publication(Status.RESUBMITTED, Duration.ofHours(1))))
                .isFalse();
        assertThat(filter.test(publication(Status.COMPLETED, Duration.ofHours(1))))
                .isFalse();
        assertThat(filter.test(publication(Status.PUBLISHED, Duration.ofHours(1))))
                .isTrue();
        assertThat(filter.test(publication(Status.PROCESSING, Duration.ofHours(1))))
                .isTrue();
        assertThat(filter.test(publication(Status.PUBLISHED, Duration.ofHours(1))))
                .isFalse();
    }

    @Test
    void skipsAdditionalRecoveryWhenInFlightLimitIsReached() {
        IncompleteEventPublications publications = mock(IncompleteEventPublications.class);
        EventPublicationRepository repository = mock(EventPublicationRepository.class);
        when(repository.countByStatus(Status.RESUBMITTED)).thenReturn(25);
        new OutboxEventResubmissionStartup(publications, properties(), repository).onApplicationReady();

        verify(publications).resubmitIncompletePublications(any(ResubmissionOptions.class));
        verify(publications, never()).resubmitIncompletePublications(any(Predicate.class));
    }

    @Test
    void scheduledRecoveryRunsRepeatedly() {
        assertThat(ModulithApplication.class.isAnnotationPresent(EnableScheduling.class))
                .isTrue();
        IncompleteEventPublications publications = mock(IncompleteEventPublications.class);
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(new MapPropertySource("test", Map.of("application.resubmission-min-age", "20ms")));
            context.register(SchedulingConfiguration.class, OutboxEventResubmissionStartup.class);
            context.registerBean(IncompleteEventPublications.class, () -> publications);
            context.registerBean(ApplicationProperties.class, this::properties);
            context.registerBean(EventPublicationRepository.class, () -> mock(EventPublicationRepository.class));
            context.refresh();
            verify(publications, timeout(5000).atLeast(2))
                    .resubmitIncompletePublications(any(ResubmissionOptions.class));
        }
    }

    private ApplicationProperties properties() {
        return new ApplicationProperties("order-created", null, 50, 100, 75, 25, Duration.ofMinutes(10));
    }

    private EventPublication publication(Status status, Duration age) {
        EventPublication publication = mock(EventPublication.class);
        when(publication.getStatus()).thenReturn(status);
        when(publication.getPublicationDate()).thenReturn(Instant.now().minus(age));
        return publication;
    }
}
