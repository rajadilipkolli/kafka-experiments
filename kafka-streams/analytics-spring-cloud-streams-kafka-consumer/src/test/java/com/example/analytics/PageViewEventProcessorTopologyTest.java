/* Licensed under Apache-2.0 2026 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.configuration.AnalyticsConsumerConstants;
import com.example.analytics.model.PageViewEvent;
import com.example.analytics.processor.PageViewEventProcessor;
import com.example.analytics.util.JsonSerdeUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageViewEventProcessorTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PageViewEvent> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;

    @BeforeEach
    void setUp() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "page-view-processor-test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, PageViewEvent> inputStream =
                builder.stream(
                        "pvs",
                        Consumed.with(
                                Serdes.String(), JsonSerdeUtils.jsonSerde(PageViewEvent.class)));

        KStream<String, Long> outputStream =
                new PageViewEventProcessor().processInput().apply(inputStream);

        outputStream.to("pcs", Produced.with(Serdes.String(), Serdes.Long()));

        Topology topology = builder.build();
        testDriver = new TopologyTestDriver(topology, props);

        inputTopic =
                testDriver.createInputTopic(
                        "pvs",
                        Serdes.String().serializer(),
                        JsonSerdeUtils.jsonSerde(PageViewEvent.class).serializer());

        outputTopic =
                testDriver.createOutputTopic(
                        "pcs", Serdes.String().deserializer(), Serdes.Long().deserializer());
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void testCumulativeCountsAndStableCardinalityWithIncrementalReads() {
        List<String> pages = List.of("home", "products", "checkout");
        int rounds = 20;
        Map<String, Long> expectedCounts = new HashMap<>();
        pages.forEach(p -> expectedCounts.put(p, 0L));

        for (int i = 0; i < rounds; i++) {
            for (String page : pages) {
                // send event with duration > 10 (qualifies)
                inputTopic.pipeInput(page, new PageViewEvent("user-" + i, page, 25));
                expectedCounts.put(page, expectedCounts.get(page) + 1L);

                // Incremental read
                KeyValue<String, Long> outputRecord = outputTopic.readKeyValue();
                assertThat(outputRecord.key).isEqualTo(page);
                assertThat(outputRecord.value).isEqualTo(expectedCounts.get(page));
            }
        }

        // Also send filtered events with duration <= 10, which should be ignored
        inputTopic.pipeInput("home", new PageViewEvent("user-short", "home", 5));
        assertThat(outputTopic.isEmpty()).isTrue();

        // Verify state store exact cumulative counts and stable key cardinality
        ReadOnlyKeyValueStore<String, Long> store =
                testDriver.getKeyValueStore(AnalyticsConsumerConstants.PAGE_COUNT_MV);

        int keyCardinality = 0;
        try (KeyValueIterator<String, Long> iterator = store.all()) {
            while (iterator.hasNext()) {
                KeyValue<String, Long> kv = iterator.next();
                keyCardinality++;
                assertThat(kv.value).isEqualTo((long) rounds);
                assertThat(pages).contains(kv.key);
            }
        }
        assertThat(keyCardinality).isEqualTo(pages.size());
    }
}
