/* Licensed under Apache-2.0 2025 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.model.PageViewEvent;
import com.example.analytics.util.JsonSerdeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdvancedStreamsOperationsTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PageViewEvent> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Configure Kafka Streams for testing
    Properties props;

    @BeforeEach
    void setUp() {
        try {
            props = new Properties();
            props.put(StreamsConfig.APPLICATION_ID_CONFIG, "advanced-operations-test");
            props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
            props.put(
                    StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                    Serdes.String().getClass().getName());
            props.put(
                    StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                    JsonSerdeUtils.getJsonClass().getName());
            props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10); // Lower for tests
            props.put(
                    StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                    LogAndContinueExceptionHandler.class.getName());

            // Build the topology for testing
            StreamsBuilder builder = new StreamsBuilder();

            // Use the common JsonSerdeUtils
            Serde<PageViewEvent> pageViewSerde =
                    JsonSerdeUtils.jsonSerde(PageViewEvent.class, objectMapper);

            // Create a KStream from the input topic
            KStream<String, PageViewEvent> pageViewStream =
                    builder.stream("page-views", Consumed.with(Serdes.String(), pageViewSerde));

            // Perform grouping and aggregation (summing durations)
            pageViewStream
                    .selectKey((key, value) -> value.getUserId())
                    .groupByKey()
                    .aggregate(
                            () -> 0L, // Initial value
                            (key, value, aggregate) -> aggregate + value.getDuration(),
                            Materialized.with(Serdes.String(), Serdes.Long()))
                    .toStream()
                    .to("user-total-duration", Produced.with(Serdes.String(), Serdes.Long()));

            // Create test driver and topics
            testDriver = new TopologyTestDriver(builder.build(), props);

            inputTopic =
                    testDriver.createInputTopic(
                            "page-views", Serdes.String().serializer(), pageViewSerde.serializer());

            outputTopic =
                    testDriver.createOutputTopic(
                            "user-total-duration",
                            Serdes.String().deserializer(),
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
    void testUserDurationAggregation() {
        // Send page view events for different users
        inputTopic.pipeInput("1", new PageViewEvent("user1", "home", 30));
        inputTopic.pipeInput("2", new PageViewEvent("user2", "products", 40));
        inputTopic.pipeInput("3", new PageViewEvent("user1", "cart", 20));
        inputTopic.pipeInput("4", new PageViewEvent("user3", "home", 60));
        inputTopic.pipeInput("5", new PageViewEvent("user2", "checkout", 50));

        // Verify aggregated durations for each user
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("user1", 30L));
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("user2", 40L));
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("user1", 50L)); // 30 + 20
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("user3", 60L));
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("user2", 90L)); // 40 + 50
    }

    @Disabled("Time windowed tests are implemented in WindowingAndErrorHandlingTest")
    @Test
    void testTimeWindowedAggregation() {
        // This is a placeholder - see WindowingAndErrorHandlingTest for time window tests
    }

    @Test
    void testBranchingOperations() {
        // Create a new topology with split/branch operations (replacement for deprecated branch())
        StreamsBuilder builder = new StreamsBuilder();
        Serde<PageViewEvent> pageViewSerde =
                JsonSerdeUtils.jsonSerde(PageViewEvent.class, objectMapper);

        // Get the page view stream
        KStream<String, PageViewEvent> pageViewStream =
                builder.stream("page-views", Consumed.with(Serdes.String(), pageViewSerde));

        // Use split() and branch mapping
        var branched =
                pageViewStream
                        .split()
                        .branch(
                                (key, value) -> value.getDuration() < 30,
                                Branched.withConsumer(
                                        ks ->
                                                ks.to(
                                                        "short-duration",
                                                        Produced.with(
                                                                Serdes.String(), pageViewSerde))))
                        .branch(
                                (key, value) ->
                                        value.getDuration() >= 30 && value.getDuration() < 60,
                                Branched.withConsumer(
                                        ks ->
                                                ks.to(
                                                        "medium-duration",
                                                        Produced.with(
                                                                Serdes.String(), pageViewSerde))))
                        .branch(
                                (key, value) -> value.getDuration() >= 60,
                                Branched.withConsumer(
                                        ks ->
                                                ks.to(
                                                        "long-duration",
                                                        Produced.with(
                                                                Serdes.String(), pageViewSerde))))
                        .noDefaultBranch();

        // Create a new test driver with this topology
        TopologyTestDriver branchTestDriver = null;
        try {
            branchTestDriver = new TopologyTestDriver(builder.build(), props);

            // Create test topics
            TestInputTopic<String, PageViewEvent> branchInputTopic =
                    branchTestDriver.createInputTopic(
                            "page-views", Serdes.String().serializer(), pageViewSerde.serializer());

            TestOutputTopic<String, PageViewEvent> shortDurationTopic =
                    branchTestDriver.createOutputTopic(
                            "short-duration",
                            Serdes.String().deserializer(),
                            pageViewSerde.deserializer());

            TestOutputTopic<String, PageViewEvent> mediumDurationTopic =
                    branchTestDriver.createOutputTopic(
                            "medium-duration",
                            Serdes.String().deserializer(),
                            pageViewSerde.deserializer());

            TestOutputTopic<String, PageViewEvent> longDurationTopic =
                    branchTestDriver.createOutputTopic(
                            "long-duration",
                            Serdes.String().deserializer(),
                            pageViewSerde.deserializer());

            // Test branching with different durations
            PageViewEvent shortEvent = new PageViewEvent("user1", "home", 20);
            PageViewEvent mediumEvent = new PageViewEvent("user2", "products", 45);
            PageViewEvent longEvent = new PageViewEvent("user3", "checkout", 75);

            branchInputTopic.pipeInput("1", shortEvent);
            branchInputTopic.pipeInput("2", mediumEvent);
            branchInputTopic.pipeInput("3", longEvent);

            // Verify each branch received the correct events
            List<PageViewEvent> shortEvents = shortDurationTopic.readValuesToList();
            List<PageViewEvent> mediumEvents = mediumDurationTopic.readValuesToList();
            List<PageViewEvent> longEvents = longDurationTopic.readValuesToList();

            assertThat(shortEvents).hasSize(1);
            assertThat(shortEvents.getFirst().getDuration()).isEqualTo(20);

            assertThat(mediumEvents).hasSize(1);
            assertThat(mediumEvents.getFirst().getDuration()).isEqualTo(45);

            assertThat(longEvents).hasSize(1);
            assertThat(longEvents.getFirst().getDuration()).isEqualTo(75);
        } catch (Exception e) {
            if (branchTestDriver != null) {
                branchTestDriver.close();
            }
            throw e;
        } finally {
            if (branchTestDriver != null) {
                branchTestDriver.close();
            }
        }
    }
}
