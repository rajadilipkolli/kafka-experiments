/* Licensed under Apache-2.0 2025 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.model.PageViewEvent;
import com.example.analytics.util.JsonSerdeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.api.*;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StateStoreAndInteractiveQueriesTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PageViewEvent> inputTopic;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AVG_DURATION_STORE = "avg-duration-store";

    @BeforeEach
    void setUp() {
        // Configure Kafka Streams for testing
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "state-store-test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());

        // Create a topology that processes page views and maintains average duration per page
        StreamsBuilder builder = new StreamsBuilder();

        // Create state store for average duration
        StoreBuilder<KeyValueStore<String, AvgDuration>> storeBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(AVG_DURATION_STORE),
                        Serdes.String(),
                        new AvgDurationSerde());

        builder.addStateStore(storeBuilder);

        // Process the input stream with a custom processor
        builder.stream(
                        "page-views",
                        Consumed.with(
                                Serdes.String(),
                                JsonSerdeUtils.jsonSerde(PageViewEvent.class, objectMapper)))
                .process(PageViewAverageProcessor::new, AVG_DURATION_STORE);

        // Build topology
        testDriver = new TopologyTestDriver(builder.build(), props);

        // Create input topic
        inputTopic =
                testDriver.createInputTopic(
                        "page-views",
                        Serdes.String().serializer(),
                        JsonSerdeUtils.jsonSerde(PageViewEvent.class, objectMapper).serializer());
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void testStateStore() {
        // Send some page view events
        inputTopic.pipeInput("key1", new PageViewEvent("user1", "home", 30));
        inputTopic.pipeInput("key2", new PageViewEvent("user2", "home", 60));
        inputTopic.pipeInput("key3", new PageViewEvent("user3", "products", 40));
        inputTopic.pipeInput("key4", new PageViewEvent("user4", "home", 90));
        inputTopic.pipeInput("key5", new PageViewEvent("user5", "products", 80));

        // Get the state store from the test driver
        KeyValueStore<String, AvgDuration> store = testDriver.getKeyValueStore(AVG_DURATION_STORE);

        // Verify the average durations in the state store
        AvgDuration homeDuration = store.get("home");
        assertThat(homeDuration).isNotNull();
        assertThat(homeDuration.getAvgDuration()).isEqualTo(60.0); // (30 + 60 + 90) / 3 = 60

        AvgDuration productsDuration = store.get("products");
        assertThat(productsDuration).isNotNull();
        assertThat(productsDuration.getAvgDuration()).isEqualTo(60.0); // (40 + 80) / 2 = 60
    }

    @Test
    void testInteractiveQueries() {
        // Insert page view events to populate the state store
        inputTopic.pipeInput("key1", new PageViewEvent("user1", "home", 30));
        inputTopic.pipeInput("key2", new PageViewEvent("user2", "home", 60));
        inputTopic.pipeInput("key3", new PageViewEvent("user3", "products", 40));
        inputTopic.pipeInput("key4", new PageViewEvent("user4", "home", 90));

        // Get the state store from the test driver
        KeyValueStore<String, AvgDuration> store = testDriver.getKeyValueStore(AVG_DURATION_STORE);

        // Query for specific keys
        AvgDuration homeDuration = store.get("home");
        assertThat(homeDuration).isNotNull();
        assertThat(homeDuration.getAvgDuration()).isEqualTo(60.0); // (30 + 60 + 90) / 3 = 60

        AvgDuration productsDuration = store.get("products");
        assertThat(productsDuration).isNotNull();
        assertThat(productsDuration.getAvgDuration())
                .isEqualTo(40.0); // Only one product view with 40

        // Test non-existent page
        AvgDuration cartDuration = store.get("cart");
        assertThat(cartDuration).isNull();

        // Send another event and verify store is updated
        inputTopic.pipeInput("key5", new PageViewEvent("user5", "products", 80));
        productsDuration = store.get("products");
        assertThat(productsDuration.getAvgDuration()).isEqualTo(60.0); // (40 + 80) / 2 = 60
    }

    // Custom processor for calculating average durations
    public static class PageViewAverageProcessor
            implements Processor<String, PageViewEvent, Void, Void> {
        private ProcessorContext<Void, Void> context;
        private KeyValueStore<String, AvgDuration> avgDurationStore;

        @Override
        public void init(ProcessorContext<Void, Void> context) {
            this.context = context;
            this.avgDurationStore = context.getStateStore(AVG_DURATION_STORE);
        }

        @Override
        public void process(Record<String, PageViewEvent> record) {
            PageViewEvent event = record.value();
            String page = event.getPage();
            long duration = event.getDuration();

            // Update the average duration for the page
            AvgDuration avgDuration = avgDurationStore.get(page);
            if (avgDuration == null) {
                avgDuration = new AvgDuration();
            }
            avgDuration.addValue(duration);
            avgDurationStore.put(page, avgDuration);
        }

        @Override
        public void close() {
            // Nothing to clean up
        }
    }

    // Value object for tracking average durations
    public static class AvgDuration {
        long totalDuration = 0;
        int count = 0;

        public void addValue(long duration) {
            totalDuration += duration;
            count++;
        }

        public double getAvgDuration() {
            return count > 0 ? (double) totalDuration / count : 0;
        }
    }

    // Custom Serde implementation for AvgDuration objects
    public static class AvgDurationSerde implements Serde<AvgDuration> {
        @Override
        public Serializer<AvgDuration> serializer() {
            return new Serializer<AvgDuration>() {
                @Override
                public byte[] serialize(String topic, AvgDuration data) {
                    if (data == null) return null;

                    // Simple serialization: totalDuration(long) + count(int)
                    byte[] bytes = new byte[12]; // 8 bytes for long + 4 bytes for int

                    // Write totalDuration (long - 8 bytes)
                    for (int i = 0; i < 8; i++) {
                        bytes[i] = (byte) ((data.totalDuration >> (8 * (7 - i))) & 0xFF);
                    }

                    // Write count (int - 4 bytes)
                    for (int i = 0; i < 4; i++) {
                        bytes[i + 8] = (byte) ((data.count >> (8 * (3 - i))) & 0xFF);
                    }

                    return bytes;
                }
            };
        }

        @Override
        public Deserializer<AvgDuration> deserializer() {
            return new Deserializer<AvgDuration>() {
                @Override
                public AvgDuration deserialize(String topic, byte[] data) {
                    if (data == null) return null;

                    AvgDuration avgDuration = new AvgDuration();

                    // Read totalDuration (long - 8 bytes)
                    for (int i = 0; i < 8; i++) {
                        avgDuration.totalDuration =
                                (avgDuration.totalDuration << 8) | (data[i] & 0xFF);
                    }

                    // Read count (int - 4 bytes)
                    for (int i = 0; i < 4; i++) {
                        avgDuration.count = (avgDuration.count << 8) | (data[i + 8] & 0xFF);
                    }

                    return avgDuration;
                }
            };
        }
    }
}
