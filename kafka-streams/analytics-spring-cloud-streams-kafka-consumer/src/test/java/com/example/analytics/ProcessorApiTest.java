/* Licensed under Apache-2.0 2025 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.model.PageViewEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.processor.*;
import org.apache.kafka.streams.state.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProcessorApiTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PageViewEvent> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;

    private static final String STATE_STORE_NAME = "page-count-store";

    @BeforeEach
    void setUp() {
        // Configure Kafka Streams for testing
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "processor-api-test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());

        // Create topology using the Processor API
        final Topology topology = new Topology();

        // Add source node
        topology.addSource(
                "PageViewSource",
                new StringDeserializer(),
                new JsonDeserializer<>(PageViewEvent.class),
                "page-views");

        // Add the processor node
        topology.addProcessor("PageViewProcessor", PageViewProcessor::new, "PageViewSource");

        // Add state store
        topology.addStateStore(
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(STATE_STORE_NAME),
                        Serdes.String(),
                        Serdes.Long()),
                "PageViewProcessor");

        // Add sink node
        topology.addSink(
                "PageCountSink",
                "page-counts",
                new StringSerializer(),
                new LongSerializer(),
                "PageViewProcessor");

        // Create the test driver
        testDriver = new TopologyTestDriver(topology, props);

        // Create test topics
        inputTopic =
                testDriver.createInputTopic(
                        "page-views",
                        new StringSerializer(),
                        new JsonSerializer<>(PageViewEvent.class));

        outputTopic =
                testDriver.createOutputTopic(
                        "page-counts", new StringDeserializer(), new LongDeserializer());
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void testProcessorWithStateStore() {
        // Send page view events
        inputTopic.pipeInput("key1", new PageViewEvent("user1", "home", 30));
        inputTopic.pipeInput("key2", new PageViewEvent("user2", "home", 45));
        inputTopic.pipeInput("key3", new PageViewEvent("user1", "products", 20));
        inputTopic.pipeInput("key4", new PageViewEvent("user3", "home", 60));

        // Check output from the processor
        KeyValue<String, Long> output1 = outputTopic.readKeyValue();
        assertThat(output1.key).isEqualTo("home");
        assertThat(output1.value).isEqualTo(1L);

        KeyValue<String, Long> output2 = outputTopic.readKeyValue();
        assertThat(output2.key).isEqualTo("home");
        assertThat(output2.value).isEqualTo(2L);

        KeyValue<String, Long> output3 = outputTopic.readKeyValue();
        assertThat(output3.key).isEqualTo("products");
        assertThat(output3.value).isEqualTo(1L);

        KeyValue<String, Long> output4 = outputTopic.readKeyValue();
        assertThat(output4.key).isEqualTo("home");
        assertThat(output4.value).isEqualTo(3L);
    }

    // Custom Processor implementation using the older API style
    public static class PageViewProcessor implements Processor {
        private ProcessorContext context;
        private KeyValueStore<String, Long> pageCountStore;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.pageCountStore = context.getStateStore(STATE_STORE_NAME);
        }

        @Override
        public void process(Object key, Object value) {
            if (!(value instanceof PageViewEvent)) return;

            // Extract the page from the event
            final PageViewEvent event = (PageViewEvent) value;
            final String page = event.getPage();

            // Update count in state store
            Long count = pageCountStore.get(page);
            if (count == null) {
                count = 0L;
            }
            count += 1;
            pageCountStore.put(page, count);

            // Forward updated count to the next processor or sink
            context.forward(page, count);
        }

        @Override
        public void close() {
            // Nothing to do
        }
    }

    // Helper classes for JSON serialization/deserialization
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
