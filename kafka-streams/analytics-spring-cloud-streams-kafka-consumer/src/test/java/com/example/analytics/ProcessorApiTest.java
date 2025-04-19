/* Licensed under Apache-2.0 2025 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.ProcessorApiTest.PageViewProcessor;
import com.example.analytics.model.PageViewEvent;
import com.example.analytics.util.JsonSerdeUtils;
import java.util.Properties;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcessorApiTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PageViewEvent> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;

    private static final String STATE_STORE_NAME = "page-count-store";

    @BeforeEach
    void setUp() {
        try {
            // Configure Kafka Streams for testing
            Properties props = new Properties();
            props.put(StreamsConfig.APPLICATION_ID_CONFIG, "processor-api-test");
            props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
            props.put(
                    StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                    Serdes.String().getClass().getName());
            props.put(
                    StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                    JsonSerdeUtils.getJsonClass().getName());

            // Create topology with processor API
            Topology topology = new Topology();

            // Add source node
            topology.addSource(
                    "PageViewSource",
                    Serdes.String().deserializer(),
                    JsonSerdeUtils.jsonSerde(PageViewEvent.class).deserializer(),
                    "page-views");

            // Add processor node
            topology.addProcessor(
                    "PageViewProcessor", () -> new PageViewProcessor(), "PageViewSource");

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
                            JsonSerdeUtils.jsonSerde(PageViewEvent.class).serializer());

            outputTopic =
                    testDriver.createOutputTopic(
                            "page-counts", new StringDeserializer(), new LongDeserializer());
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
    void testProcessorWithStateStore() {
        // Send some page view events
        inputTopic.pipeInput("key1", new PageViewEvent("user1", "home", 100));
        inputTopic.pipeInput("key2", new PageViewEvent("user2", "home", 200));
        inputTopic.pipeInput("key3", new PageViewEvent("user3", "products", 150));
        inputTopic.pipeInput("key4", new PageViewEvent("user4", "home", 300));
        inputTopic.pipeInput("key5", new PageViewEvent("user5", "cart", 400));

        // Verify the expected counts for each page
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("home", 1L));
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("home", 2L));
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("products", 1L));
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("home", 3L));
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("cart", 1L));
    }

    // Custom Processor implementation using the older API style
    public static class PageViewProcessor
            implements Processor<String, PageViewEvent, String, Long> {
        private ProcessorContext<String, Long> context;
        private KeyValueStore<String, Long> pageCountStore;

        @Override
        public void init(ProcessorContext<String, Long> context) {
            this.context = context;
            this.pageCountStore = context.getStateStore(STATE_STORE_NAME);
        }

        @Override
        public void process(Record<String, PageViewEvent> record) {
            PageViewEvent pageViewEvent = record.value();
            if (pageViewEvent == null) return;
            String page = pageViewEvent.getPage();
            // Update count in state store
            Long count = pageCountStore.get(page);
            if (count == null) {
                count = 0L;
            }
            count += 1;
            pageCountStore.put(page, count);

            // Forward updated count to the next processor or sink
            context.forward(new Record<>(page, count, record.timestamp()));
        }
    }
}
