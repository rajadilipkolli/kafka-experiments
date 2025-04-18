/* Licensed under Apache-2.0 2025 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.model.PageViewEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Properties;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdvancedStreamsOperationsTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PageViewEvent> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;

    @BeforeEach
    void setUp() {
        // Configure Kafka Streams for testing
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "advanced-streams-test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());

        // Build the topology
        StreamsBuilder builder = new StreamsBuilder();

        // Create an input topic for PageViewEvents
        KStream<String, PageViewEvent> pageViewStream =
                builder.stream(
                        "page-views",
                        Consumed.with(Serdes.String(), new JsonSerde<>(PageViewEvent.class)));

        // 1. Demonstrate time windowing aggregation
        pageViewStream
                .groupBy(
                        (key, value) -> value.getPage(),
                        Grouped.with(Serdes.String(), new JsonSerde<>(PageViewEvent.class)))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                .count()
                .toStream()
                .map(
                        (windowedKey, count) ->
                                KeyValue.pair(
                                        windowedKey.key()
                                                + "@"
                                                + windowedKey.window().start()
                                                + "-"
                                                + windowedKey.window().end(),
                                        count))
                .to("page-counts-windowed", Produced.with(Serdes.String(), Serdes.Long()));

        // 2. Filtering and branching example
        KStream<String, PageViewEvent>[] branches =
                pageViewStream.branch(
                        (key, event) -> event.getDuration() > 60, // Long visits (>1 min)
                        (key, event) ->
                                event.getDuration() > 10
                                        && event.getDuration() <= 60, // Medium visits
                        (key, event) -> event.getDuration() <= 10 // Short visits
                        );

        // Long visits
        branches[0]
                .mapValues(event -> event.getDuration())
                .to("long-visits", Produced.with(Serdes.String(), Serdes.Long()));

        // Medium visits
        branches[1]
                .mapValues(event -> event.getDuration())
                .to("medium-visits", Produced.with(Serdes.String(), Serdes.Long()));

        // Short visits
        branches[2]
                .mapValues(event -> event.getDuration())
                .to("short-visits", Produced.with(Serdes.String(), Serdes.Long()));

        // 3. Aggregating total duration per user
        pageViewStream
                .groupBy(
                        (key, value) -> value.getUserId(),
                        Grouped.with(Serdes.String(), new JsonSerde<>(PageViewEvent.class)))
                .aggregate(
                        () -> 0L, // initializer
                        (userId, event, total) -> total + event.getDuration(), // aggregator
                        Materialized.with(Serdes.String(), Serdes.Long()))
                .toStream()
                .to("user-total-duration", Produced.with(Serdes.String(), Serdes.Long()));

        // Create the topology and test driver
        Topology topology = builder.build();
        testDriver = new TopologyTestDriver(topology, props);

        // Setup input and output topics
        inputTopic =
                testDriver.createInputTopic(
                        "page-views",
                        Serdes.String().serializer(),
                        new JsonSerde<>(PageViewEvent.class).serializer());

        outputTopic =
                testDriver.createOutputTopic(
                        "user-total-duration",
                        Serdes.String().deserializer(),
                        Serdes.Long().deserializer());
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void testUserDurationAggregation() {
        // Send page views for the same user
        String userId = "user123";

        inputTopic.pipeInput(userId, new PageViewEvent(userId, "home", 30));
        inputTopic.pipeInput(userId, new PageViewEvent(userId, "products", 45));
        inputTopic.pipeInput(userId, new PageViewEvent(userId, "cart", 15));

        // Verify the user's total duration is calculated correctly
        KeyValue<String, Long> output = outputTopic.readKeyValue();
        assertThat(output.key).isEqualTo(userId);
        assertThat(output.value).isEqualTo(30L);

        output = outputTopic.readKeyValue();
        assertThat(output.key).isEqualTo(userId);
        assertThat(output.value).isEqualTo(75L);

        output = outputTopic.readKeyValue();
        assertThat(output.key).isEqualTo(userId);
        assertThat(output.value).isEqualTo(90L);
    }

    // Helper class for JSON serialization/deserialization
    public static class JsonSerde<T> implements Serde<T> {
        private final JsonSerializer<T> serializer;
        private final JsonDeserializer<T> deserializer;

        public JsonSerde(Class<T> cls) {
            this.serializer = new JsonSerializer<>(cls);
            this.deserializer = new JsonDeserializer<>(cls);
        }

        @Override
        public Serializer<T> serializer() {
            return serializer;
        }

        @Override
        public Deserializer<T> deserializer() {
            return deserializer;
        }
    }

    public static class JsonSerializer<T> implements Serializer<T> {
        private final ObjectMapper mapper = new ObjectMapper();
        private final Class<T> cls;

        public JsonSerializer(Class<T> cls) {
            this.cls = cls;
        }

        @Override
        public byte[] serialize(String topic, T data) {
            if (data == null) return null;
            try {
                return mapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class JsonDeserializer<T> implements Deserializer<T> {
        private final ObjectMapper mapper = new ObjectMapper();
        private final Class<T> cls;

        public JsonDeserializer(Class<T> cls) {
            this.cls = cls;
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            if (data == null) return null;
            try {
                return mapper.readValue(data, cls);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
