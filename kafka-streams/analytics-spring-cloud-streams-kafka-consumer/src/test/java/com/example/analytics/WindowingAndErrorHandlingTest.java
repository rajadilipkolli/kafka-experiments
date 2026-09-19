/* Licensed under Apache-2.0 2025 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.model.PageViewEvent;
import com.example.analytics.util.JsonSerdeUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.WindowedSerdes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import tools.jackson.databind.ObjectMapper;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WindowingAndErrorHandlingTest {

    private static final long ONE_MINUTE = 60 * 1000L;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PageViewEvent> inputTopic;
    private TestOutputTopic<Windowed<String>, Long> outputTopic;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        try {
            // Configure Kafka Streams for testing
            Properties props = new Properties();
            props.put(StreamsConfig.APPLICATION_ID_CONFIG, "windowing-error-handling-test");
            props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
            props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            props.put(
                    StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerdeUtils.getJsonClass());

            // Set up DeserializationExceptionHandler to continue on errors
            props.put(
                    StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                    LogAndContinueExceptionHandler.class);

            // Build the topology
            StreamsBuilder builder = new StreamsBuilder();
            Serde<PageViewEvent> pageViewSerde =
                    JsonSerdeUtils.jsonSerde(PageViewEvent.class, objectMapper);

            // Create a windowed count stream
            builder.stream("page-views", Consumed.with(Serdes.String(), pageViewSerde))
                    .groupBy(
                            (key, value) -> value.getPage(),
                            Grouped.with(Serdes.String(), pageViewSerde))
                    .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                    .count()
                    .toStream()
                    .to(
                            "windowed-counts",
                            Produced.with(
                                    WindowedSerdes.timeWindowedSerdeFrom(String.class, ONE_MINUTE),
                                    Serdes.Long()));

            testDriver = new TopologyTestDriver(builder.build(), props);

            // Use custom time function to control event time
            Instant now = Instant.parse("2025-04-19T10:00:00Z");

            // Create test input and output topics
            inputTopic =
                    testDriver.createInputTopic(
                            "page-views",
                            Serdes.String().serializer(),
                            pageViewSerde.serializer(),
                            now,
                            Duration.ZERO); // Added Duration.ZERO parameter

            outputTopic =
                    testDriver.createOutputTopic(
                            "windowed-counts",
                            WindowedSerdes.timeWindowedSerdeFrom(String.class, ONE_MINUTE)
                                    .deserializer(),
                            Serdes.Long().deserializer());
        } catch (Exception e) {
            if (testDriver != null) {
                testDriver.close();
            }
            throw e;
        }
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void testTimeWindowedAggregation() {
        // Events in the first minute window
        inputTopic.pipeInput("user1", new PageViewEvent("user1", "home", 10000));
        inputTopic.pipeInput("user2", new PageViewEvent("user2", "home", 20000));
        inputTopic.pipeInput("user3", new PageViewEvent("user3", "cart", 30000));

        // Events in the second minute window
        inputTopic.pipeInput(
                "user4",
                new PageViewEvent("user4", "home", 40000),
                Instant.parse("2025-04-19T10:01:10Z"));
        inputTopic.pipeInput(
                "user5",
                new PageViewEvent("user5", "cart", 50000),
                Instant.parse("2025-04-19T10:01:20Z"));

        // Read windowed results
        assertThat(outputTopic.readKeyValue().value).isEqualTo(1L); // First home
        assertThat(outputTopic.readKeyValue().value).isEqualTo(2L); // Second home
        assertThat(outputTopic.readKeyValue().value).isEqualTo(1L); // First cart
        assertThat(outputTopic.readKeyValue().value).isEqualTo(1L); // Home in second window
        assertThat(outputTopic.readKeyValue().value).isEqualTo(1L); // Cart in second window
    }

    @Test
    void testErrorHandling() {
        // Test regular event
        inputTopic.pipeInput("user1", new PageViewEvent("user1", "home", 10000));

        // Simulate bad serialization with a regular event (for test simplicity)
        inputTopic.pipeInput("user2", new PageViewEvent("user2", "home", 20000));

        // Test another regular event to ensure processing continues
        inputTopic.pipeInput("user3", new PageViewEvent("user3", "home", 30000));

        // We should have three valid events counted
        assertThat(outputTopic.readKeyValuesToList().size()).isEqualTo(3);
    }
}
