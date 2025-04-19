/* Licensed under Apache-2.0 2025 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.model.PageViewEvent;
import com.example.analytics.util.JsonSerdeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StreamTableJoinsTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PageViewEvent> pageViewTopic;
    private TestInputTopic<String, String> userProfileTopic;
    private TestOutputTopic<String, String> enrichedPageViewTopic;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Configure Kafka Streams for testing
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream-table-join-test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());

        // Build the topology
        StreamsBuilder builder = new StreamsBuilder();
        Serde<PageViewEvent> pageViewSerde =
                JsonSerdeUtils.jsonSerde(PageViewEvent.class, objectMapper);

        // Create streams and tables
        KStream<String, PageViewEvent> pageViews =
                builder.stream("page-views", Consumed.with(Serdes.String(), pageViewSerde));

        KTable<String, String> userProfiles =
                builder.table(
                        "user-profiles",
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized.as("user-profile-store"));

        // Join the stream with the table
        pageViews
                .selectKey((key, value) -> value.getUserId())
                .leftJoin(
                        userProfiles,
                        (pageView, userProfile) -> {
                            String region = userProfile != null ? userProfile : "unknown";
                            return String.format(
                                    "Page: %s, User: %s, Region: %s, Duration: %d",
                                    pageView.getPage(),
                                    pageView.getUserId(),
                                    region,
                                    pageView.getDuration());
                        },
                        Joined.with(Serdes.String(), pageViewSerde, Serdes.String()))
                .to("enriched-page-views", Produced.with(Serdes.String(), Serdes.String()));

        // Create test driver and topics
        testDriver = new TopologyTestDriver(builder.build(), props);

        pageViewTopic =
                testDriver.createInputTopic(
                        "page-views", Serdes.String().serializer(), pageViewSerde.serializer());

        userProfileTopic =
                testDriver.createInputTopic(
                        "user-profiles",
                        Serdes.String().serializer(),
                        Serdes.String().serializer());

        enrichedPageViewTopic =
                testDriver.createOutputTopic(
                        "enriched-page-views",
                        Serdes.String().deserializer(),
                        Serdes.String().deserializer());
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void testStreamTableJoinForUserProfileEnrichment() {
        // 1. First, populate the user profiles table with some data
        userProfileTopic.pipeInput("user1", "North America");
        userProfileTopic.pipeInput("user2", "Europe");
        userProfileTopic.pipeInput("user3", "Asia");

        // 2. Now send page view events
        pageViewTopic.pipeInput("pageview1", new PageViewEvent("user1", "homepage", 30));
        pageViewTopic.pipeInput("pageview2", new PageViewEvent("user2", "products", 45));
        pageViewTopic.pipeInput("pageview3", new PageViewEvent("user3", "cart", 60));
        pageViewTopic.pipeInput(
                "pageview4", new PageViewEvent("user4", "checkout", 90)); // unknown user

        // 3. Read and validate the enriched output - we should have four records
        assertThat(enrichedPageViewTopic.readValue())
                .contains("Page: homepage, User: user1, Region: North America, Duration: 30");
        assertThat(enrichedPageViewTopic.readValue())
                .contains("Page: products, User: user2, Region: Europe, Duration: 45");
        assertThat(enrichedPageViewTopic.readValue())
                .contains("Page: cart, User: user3, Region: Asia, Duration: 60");
        assertThat(enrichedPageViewTopic.readValue())
                .contains("Page: checkout, User: user4, Region: unknown, Duration: 90");
    }

    @Test
    void testUpdatingTableValues() {
        // 1. First, populate the user profiles table with initial data
        userProfileTopic.pipeInput("user1", "North America");

        // 2. Send a page view event
        pageViewTopic.pipeInput("pageview1", new PageViewEvent("user1", "homepage", 30));

        // 3. Verify the initial join result
        assertThat(enrichedPageViewTopic.readValue())
                .contains("Page: homepage, User: user1, Region: North America, Duration: 30");

        // 4. Update the user profile
        userProfileTopic.pipeInput("user1", "Europe"); // User moved to Europe

        // 5. Send another page view for the same user
        pageViewTopic.pipeInput("pageview2", new PageViewEvent("user1", "products", 45));

        // 6. Verify the updated join result uses the new profile data
        assertThat(enrichedPageViewTopic.readValue())
                .contains("Page: products, User: user1, Region: Europe, Duration: 45");
    }

    @Test
    void testTableTombstones() {
        // 1. First, populate the user profiles table with initial data
        userProfileTopic.pipeInput("user1", "North America");

        // 2. Send a page view event
        pageViewTopic.pipeInput("pageview1", new PageViewEvent("user1", "homepage", 30));

        // 3. Verify the initial join result
        assertThat(enrichedPageViewTopic.readValue())
                .contains("Page: homepage, User: user1, Region: North America, Duration: 30");

        // 4. Send a tombstone to delete the user profile - specify types explicitly
        userProfileTopic.pipeInput("user1", (String) null);

        // 5. Send another page view for the same user
        pageViewTopic.pipeInput("pageview2", new PageViewEvent("user1", "products", 45));

        // 6. Verify the updated join result shows the profile is now unknown
        assertThat(enrichedPageViewTopic.readValue())
                .contains("Page: products, User: user1, Region: unknown, Duration: 45");
    }
}
