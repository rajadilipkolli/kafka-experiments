package com.example.kafka.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

class ToUpperStringProcessorTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(ProcessorConfiguration.class);

    @Configuration(proxyBeanMethods = false)
    @Import(ToUpperStringProcessor.class)
    static class ProcessorConfiguration {}

    @Test
    void providerCreatesIndependentProcessorsWithConfiguredHistoryCapacity() {
        contextRunner.withPropertyValues("app.processor.history-capacity=2").run(context -> {
            var provider = context.getBeanProvider(ToUpperStringProcessor.class);
            ToUpperStringProcessor processor = provider.getObject("consumer-1");
            ToUpperStringProcessor other = provider.getObject("consumer-2");
            processor.processString("first");
            processor.processString("second");
            processor.processString("third");

            assertThat(processor.distinctQueuedData()).containsExactlyInAnyOrder("SECOND", "THIRD");
            assertThat(processor.processedCount()).isEqualTo(3);
            assertThat(other).isNotSameAs(processor);
            assertThat(other.queueSize()).isZero();
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsNonPositiveCapacityWhenProviderCreatesProcessor(int capacity) {
        contextRunner
                .withPropertyValues("app.processor.history-capacity=" + capacity)
                .run(context -> {
                    assertThatThrownBy(() -> context.getBeanProvider(ToUpperStringProcessor.class)
                                    .getObject("consumer"))
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .hasRootCauseMessage("app.processor.history-capacity must be positive");
                });
    }
}
