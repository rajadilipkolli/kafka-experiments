/* Licensed under Apache-2.0 2025 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.model.PageViewEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
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

        // Create a JsonSerde for PageViewEvent
        JsonSerde<PageViewEvent> pageViewSerde = new JsonSerde<>(PageViewEvent.class);

        // Create a KStream for page views
        KStream<String, PageViewEvent> pageViews =
                builder.stream("page-views", Consumed.with(Serdes.String(), pageViewSerde));

        // Create a KTable for user profiles - a simple mapping of userId to user region/category
        KTable<String, String> userProfiles =
                builder.table(
                        "user-profiles",
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized.as("user-profile-store"));

        // Join the stream of page views with the table of user profiles
        KStream<String, String> enrichedPageViews =
                pageViews
                        .selectKey(
                                (key, value) ->
                                        value.getUserId()) // Repartition by userId for the join
                        .leftJoin(
                                userProfiles,
                                (pageView, userProfile) -> {
                                    // Combine page view with user profile information
                                    String region = userProfile != null ? userProfile : "unknown";
                                    return String.format(
                                            "Page: %s, User: %s, Region: %s, Duration: %d",
                                            pageView.getPage(),
                                            pageView.getUserId(),
                                            region,
                                            pageView.getDuration());
                                },
                                Joined.with(
                                        Serdes.String(), // Join key serde
                                        pageViewSerde, // Page view serde
                                        Serdes.String() // User profile serde
                                        ));

        // Output the enriched page views to a topic
        enrichedPageViews.to(
                "enriched-page-views", Produced.with(Serdes.String(), Serdes.String()));

        // Build the topology and the test driver
        Topology topology = builder.build();
        testDriver = new TopologyTestDriver(topology, props);

        // Create test input and output topics
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

    // Helper JSON serde classes
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
