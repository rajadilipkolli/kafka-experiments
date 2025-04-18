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

class StreamJoinsAndTransformationsTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PageViewEvent> pageViewTopic;
    private TestInputTopic<String, UserProfile> userProfileTopic;
    private TestOutputTopic<String, EnrichedPageView> enrichedPageViewTopic;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // User profile model for join demonstration
    static class UserProfile {
        private String userId;
        private String name;
        private String country;

        public UserProfile() {}

        public UserProfile(String userId, String name, String country) {
            this.userId = userId;
            this.name = name;
            this.country = country;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }

    // Enriched page view model after join operation
    static class EnrichedPageView {
        private String userId;
        private String userName;
        private String country;
        private String page;
        private long duration;

        public EnrichedPageView() {}

        public EnrichedPageView(
                String userId, String userName, String country, String page, long duration) {
            this.userId = userId;
            this.userName = userName;
            this.country = country;
            this.page = page;
            this.duration = duration;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getPage() {
            return page;
        }

        public void setPage(String page) {
            this.page = page;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
    }

    @BeforeEach
    void setUp() {
        // Configure Kafka Streams for testing
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "join-transform-test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());

        // Create a StreamsBuilder
        StreamsBuilder builder = new StreamsBuilder();

        // Create serde instances for custom types
        JsonSerde<PageViewEvent> pageViewSerde = new JsonSerde<>(PageViewEvent.class, objectMapper);
        JsonSerde<UserProfile> userProfileSerde = new JsonSerde<>(UserProfile.class, objectMapper);
        JsonSerde<EnrichedPageView> enrichedPageViewSerde =
                new JsonSerde<>(EnrichedPageView.class, objectMapper);

        // Create streams for page views and user profiles
        KStream<String, PageViewEvent> pageViewStream =
                builder.stream("page-views", Consumed.with(Serdes.String(), pageViewSerde));

        KTable<String, UserProfile> userProfileTable =
                builder.table("user-profiles", Consumed.with(Serdes.String(), userProfileSerde));

        // Perform a join between page views and user profiles
        KStream<String, EnrichedPageView> joinedStream =
                pageViewStream.join(
                        userProfileTable,
                        (pageView, profile) ->
                                new EnrichedPageView(
                                        pageView.getUserId(),
                                        profile.getName(),
                                        profile.getCountry(),
                                        pageView.getPage(),
                                        pageView.getDuration()));

        // Output the joined stream
        joinedStream.to(
                "enriched-page-views", Produced.with(Serdes.String(), enrichedPageViewSerde));

        // Create the topology and test driver
        Topology topology = builder.build();
        testDriver = new TopologyTestDriver(topology, props);

        // Setup test topics
        pageViewTopic =
                testDriver.createInputTopic(
                        "page-views", Serdes.String().serializer(), pageViewSerde.serializer());

        userProfileTopic =
                testDriver.createInputTopic(
                        "user-profiles",
                        Serdes.String().serializer(),
                        userProfileSerde.serializer());

        enrichedPageViewTopic =
                testDriver.createOutputTopic(
                        "enriched-page-views",
                        Serdes.String().deserializer(),
                        enrichedPageViewSerde.deserializer());
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void testPageViewUserProfileJoin() {
        // First add user profiles to the KTable
        userProfileTopic.pipeInput("user1", new UserProfile("user1", "Alice", "USA"));
        userProfileTopic.pipeInput("user2", new UserProfile("user2", "Bob", "Canada"));

        // Then send page view events
        pageViewTopic.pipeInput("user1", new PageViewEvent("user1", "home", 30));
        pageViewTopic.pipeInput("user2", new PageViewEvent("user2", "products", 45));
        pageViewTopic.pipeInput("user1", new PageViewEvent("user1", "checkout", 60));

        // Verify the join results
        EnrichedPageView result1 = enrichedPageViewTopic.readValue();
        assertThat(result1.getUserId()).isEqualTo("user1");
        assertThat(result1.getUserName()).isEqualTo("Alice");
        assertThat(result1.getCountry()).isEqualTo("USA");
        assertThat(result1.getPage()).isEqualTo("home");
        assertThat(result1.getDuration()).isEqualTo(30);

        EnrichedPageView result2 = enrichedPageViewTopic.readValue();
        assertThat(result2.getUserId()).isEqualTo("user2");
        assertThat(result2.getUserName()).isEqualTo("Bob");
        assertThat(result2.getCountry()).isEqualTo("Canada");
        assertThat(result2.getPage()).isEqualTo("products");
        assertThat(result2.getDuration()).isEqualTo(45);

        EnrichedPageView result3 = enrichedPageViewTopic.readValue();
        assertThat(result3.getUserId()).isEqualTo("user1");
        assertThat(result3.getUserName()).isEqualTo("Alice");
        assertThat(result3.getCountry()).isEqualTo("USA");
        assertThat(result3.getPage()).isEqualTo("checkout");
        assertThat(result3.getDuration()).isEqualTo(60);
    }

    @Test
    void testMissingUserProfile() {
        // Add only one user profile
        userProfileTopic.pipeInput("user1", new UserProfile("user1", "Alice", "USA"));

        // Send page views for both a known and unknown user
        pageViewTopic.pipeInput("user1", new PageViewEvent("user1", "home", 30));
        pageViewTopic.pipeInput("unknown", new PageViewEvent("unknown", "home", 20));
        pageViewTopic.pipeInput("user1", new PageViewEvent("user1", "about", 15));

        // Verify the join results - should only see results for user1
        EnrichedPageView result1 = enrichedPageViewTopic.readValue();
        assertThat(result1.getUserId()).isEqualTo("user1");
        assertThat(result1.getPage()).isEqualTo("home");

        EnrichedPageView result2 = enrichedPageViewTopic.readValue();
        assertThat(result2.getUserId()).isEqualTo("user1");
        assertThat(result2.getPage()).isEqualTo("about");

        // Should have no more results since "unknown" user didn't match
        assertThat(enrichedPageViewTopic.isEmpty()).isTrue();
    }

    // Helper class for JSON serialization/deserialization
    private static class JsonSerde<T> implements Serde<T> {
        private final ObjectMapper mapper;
        private final Class<T> cls;

        public JsonSerde(Class<T> cls, ObjectMapper mapper) {
            this.cls = cls;
            this.mapper = mapper;
        }

        @Override
        public Serializer<T> serializer() {
            return (topic, data) -> {
                try {
                    return mapper.writeValueAsBytes(data);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }

        @Override
        public Deserializer<T> deserializer() {
            return (topic, data) -> {
                try {
                    return mapper.readValue(data, cls);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }
}
