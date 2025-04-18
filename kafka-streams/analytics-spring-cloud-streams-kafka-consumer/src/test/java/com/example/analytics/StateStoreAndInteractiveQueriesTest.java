/* Licensed under Apache-2.0 2025 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.model.PageViewEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StateStoreAndInteractiveQueriesTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PageViewEvent> inputTopic;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private StreamsBuilder builder;
    private Topology topology;

    // State store names
    private static final String PAGE_COUNT_STORE = "page-count-store";
    private static final String USER_PAGE_STORE = "user-page-store";
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
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100); // Commit more frequently for tests

        // Create a StreamsBuilder
        builder = new StreamsBuilder();

        // Create serdes for custom types
        JsonSerde<PageViewEvent> pageViewSerde = new JsonSerde<>(PageViewEvent.class);

        // 1. Create a persistent key-value state store for page counts
        builder.addStateStore(
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(PAGE_COUNT_STORE),
                        Serdes.String(),
                        Serdes.Long()));

        // 2. Create a persistent key-value store for user-page relationships
        builder.addStateStore(
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(USER_PAGE_STORE),
                        Serdes.String(),
                        Serdes.String()));

        // 3. Create a session state store for tracking page view durations
        builder.addStateStore(
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(AVG_DURATION_STORE),
                        Serdes.String(),
                        new AvgDurationSerde() // Custom serde for average calculation
                        ));

        // Create the input stream
        KStream<String, PageViewEvent> pageViewStream =
                builder.stream("page-views", Consumed.with(Serdes.String(), pageViewSerde));

        // 4. Process page view events and update state stores using the processor API
        pageViewStream.process(
                () -> new PageStatsProcessor(),
                PAGE_COUNT_STORE,
                USER_PAGE_STORE,
                AVG_DURATION_STORE);

        // 5. Also demonstrate updating state through KTable aggregation
        pageViewStream
                .groupBy(
                        (key, value) -> value.getPage(),
                        Grouped.with(Serdes.String(), pageViewSerde))
                .aggregate(
                        () -> 0L, // initializer (starting value)
                        (aggKey, value, aggregate) -> aggregate + 1L, // aggregator
                        Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(
                                        "page-view-counts-ktable")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long()));

        // Build the topology
        topology = builder.build();
        testDriver = new TopologyTestDriver(topology, props);

        // Create the input topic
        inputTopic =
                testDriver.createInputTopic(
                        "page-views", Serdes.String().serializer(), pageViewSerde.serializer());
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void testPageCountStateStore() {
        // Send several page view events
        inputTopic.pipeInput("user1", new PageViewEvent("user1", "home", 30));
        inputTopic.pipeInput("user2", new PageViewEvent("user2", "home", 45));
        inputTopic.pipeInput("user1", new PageViewEvent("user1", "products", 60));
        inputTopic.pipeInput("user3", new PageViewEvent("user3", "home", 20));

        // Query the state store directly to verify counts
        KeyValueStore<String, Long> pageCountStore = testDriver.getKeyValueStore(PAGE_COUNT_STORE);

        // Check page counts
        assertThat(pageCountStore.get("home")).isEqualTo(3L);
        assertThat(pageCountStore.get("products")).isEqualTo(1L);
    }

    @Test
    void testUserPageRelationshipStore() {
        // Send page view events for different users
        inputTopic.pipeInput("user1", new PageViewEvent("user1", "home", 30));
        inputTopic.pipeInput("user2", new PageViewEvent("user2", "products", 45));
        inputTopic.pipeInput("user1", new PageViewEvent("user1", "checkout", 60));

        // Query the user-page relationship store
        KeyValueStore<String, String> userPageStore = testDriver.getKeyValueStore(USER_PAGE_STORE);

        // Check the last page viewed by each user
        assertThat(userPageStore.get("user1")).isEqualTo("checkout");
        assertThat(userPageStore.get("user2")).isEqualTo("products");
    }

    @Test
    void testAverageDurationCalculationStore() {
        // Send multiple page views for the same pages
        inputTopic.pipeInput("user1", new PageViewEvent("user1", "home", 30));
        inputTopic.pipeInput("user2", new PageViewEvent("user2", "home", 60));
        inputTopic.pipeInput("user3", new PageViewEvent("user3", "home", 90));
        inputTopic.pipeInput("user4", new PageViewEvent("user4", "products", 45));
        inputTopic.pipeInput("user5", new PageViewEvent("user5", "products", 75));

        // Query the average duration store
        KeyValueStore<String, AvgDuration> avgDurationStore =
                testDriver.getKeyValueStore(AVG_DURATION_STORE);

        // Check average durations
        assertThat(avgDurationStore.get("home").getAverage()).isEqualTo(60.0); // (30+60+90)/3 = 60
        assertThat(avgDurationStore.get("products").getAverage()).isEqualTo(60.0); // (45+75)/2 = 60
    }

    // Custom processor that updates multiple state stores
    private static class PageStatsProcessor implements Processor {
        private ProcessorContext context;
        private KeyValueStore<String, Long> pageCountStore;
        private KeyValueStore<String, String> userPageStore;
        private KeyValueStore<String, AvgDuration> avgDurationStore;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.pageCountStore =
                    (KeyValueStore<String, Long>) context.getStateStore(PAGE_COUNT_STORE);
            this.userPageStore =
                    (KeyValueStore<String, String>) context.getStateStore(USER_PAGE_STORE);
            this.avgDurationStore =
                    (KeyValueStore<String, AvgDuration>) context.getStateStore(AVG_DURATION_STORE);
        }

        @Override
        public void process(Object key, Object value) {
            if (!(value instanceof PageViewEvent)) return;

            PageViewEvent event = (PageViewEvent) value;
            if (event == null) return;

            // Update page count
            String page = event.getPage();
            Long count = pageCountStore.get(page);
            if (count == null) count = 0L;
            pageCountStore.put(page, count + 1L);

            // Update last page viewed by user
            userPageStore.put(event.getUserId(), page);

            // Update average duration for the page
            AvgDuration avgDuration = avgDurationStore.get(page);
            if (avgDuration == null) {
                avgDuration = new AvgDuration();
            }
            avgDuration.update(event.getDuration());
            avgDurationStore.put(page, avgDuration);
        }

        @Override
        public void close() {
            // Nothing to do
        }
    }

    // Value object for tracking average durations
    public static class AvgDuration {
        private long totalDuration = 0;
        private int count = 0;

        public void update(long duration) {
            totalDuration += duration;
            count++;
        }

        public double getAverage() {
            return count > 0 ? (double) totalDuration / count : 0;
        }

        public long getTotalDuration() {
            return totalDuration;
        }

        public int getCount() {
            return count;
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

    // Helper classes for JSON serialization/deserialization
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
