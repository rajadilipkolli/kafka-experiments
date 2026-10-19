package com.example.outboxpattern.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;

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

        OutboxEventResubmissionStartup startup = new OutboxEventResubmissionStartup(publications, properties);
        startup.onApplicationReady();

        ArgumentCaptor<ResubmissionOptions> captor = ArgumentCaptor.forClass(ResubmissionOptions.class);
        verify(publications).resubmitIncompletePublications(captor.capture());

        ResubmissionOptions captured = captor.getValue();
        assertThat(captured.getBatchSize()).isEqualTo(75);
        assertThat(captured.getMaxInFlight()).isEqualTo(25);
        assertThat(captured.getMinAge()).isEqualTo(Duration.ofMinutes(10));
    }
}
