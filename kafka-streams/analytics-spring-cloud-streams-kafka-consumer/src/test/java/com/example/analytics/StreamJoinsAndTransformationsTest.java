/* Licensed under Apache-2.0 2025 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.StreamJoinsAndTransformationsTest.EnrichedPageView;
import com.example.analytics.model.PageViewEvent;
import com.example.analytics.util.JsonSerdeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
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
import org.apache.kafka.streams.kstream.Produced;
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
    @Setter
    @Getter
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
    }

    // Enriched page view model after join operation
    @Setter
    @Getter
    static class EnrichedPageView {
        private String userId;
        private String userName;
        private String userCountry;
        private String pageName;
        private long duration;

        public EnrichedPageView() {}

        public EnrichedPageView(
                String userId,
                String userName,
                String userCountry,
                String pageName,
                long duration) {
            this.userId = userId;
            this.userName = userName;
            this.userCountry = userCountry;
            this.pageName = pageName;
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
        Serde<PageViewEvent> pageViewSerde =
                JsonSerdeUtils.jsonSerde(PageViewEvent.class, objectMapper);
        Serde<UserProfile> userProfileSerde =
                JsonSerdeUtils.jsonSerde(UserProfile.class, objectMapper);
        Serde<EnrichedPageView> enrichedPageViewSerde =
                JsonSerdeUtils.jsonSerde(EnrichedPageView.class, objectMapper);

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
                                        pageView.getDuration()),
                        Joined.with(Serdes.String(), pageViewSerde, userProfileSerde));

        // Output the enriched page views
        joinedStream.to(
                "enriched-page-views", Produced.with(Serdes.String(), enrichedPageViewSerde));

        // Create the test driver
        testDriver = new TopologyTestDriver(builder.build(), props);

        // Create test topics
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
        // Add user profiles to the table
        UserProfile user1 = new UserProfile("user1", "John Doe", "USA");
        UserProfile user2 = new UserProfile("user2", "Jane Smith", "Canada");
        userProfileTopic.pipeInput(user1.getUserId(), user1);
        userProfileTopic.pipeInput(user2.getUserId(), user2);

        // Send page view events
        PageViewEvent page1 = new PageViewEvent("user1", "home", 60);
        PageViewEvent page2 = new PageViewEvent("user2", "products", 120);
        pageViewTopic.pipeInput(page1.getUserId(), page1);
        pageViewTopic.pipeInput(page2.getUserId(), page2);

        // Verify the joined results
        EnrichedPageView result1 = enrichedPageViewTopic.readValue();
        assertThat(result1).isNotNull();
        assertThat(result1.getUserId()).isEqualTo("user1");
        assertThat(result1.getUserName()).isEqualTo("John Doe");
        assertThat(result1.getUserCountry()).isEqualTo("USA");
        assertThat(result1.getPageName()).isEqualTo("home");
        assertThat(result1.getDuration()).isEqualTo(60);

        EnrichedPageView result2 = enrichedPageViewTopic.readValue();
        assertThat(result2).isNotNull();
        assertThat(result2.getUserId()).isEqualTo("user2");
        assertThat(result2.getUserName()).isEqualTo("Jane Smith");
        assertThat(result2.getUserCountry()).isEqualTo("Canada");
        assertThat(result2.getPageName()).isEqualTo("products");
        assertThat(result2.getDuration()).isEqualTo(120);
    }

    @Test
    void testMissingUserProfile() {
        // Add just one user profile
        UserProfile user1 = new UserProfile("user1", "John Doe", "USA");
        userProfileTopic.pipeInput(user1.getUserId(), user1);

        // Send two page views but only one has a matching profile
        PageViewEvent page1 = new PageViewEvent("user1", "home", 60);
        PageViewEvent page2 = new PageViewEvent("user3", "cart", 90); // no profile for user3
        pageViewTopic.pipeInput(page1.getUserId(), page1);
        pageViewTopic.pipeInput(page2.getUserId(), page2);

        // Only one result should be produced (inner join)
        EnrichedPageView result = enrichedPageViewTopic.readValue();
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user1");

        // There should be no more records since user3 had no profile
        assertThat(enrichedPageViewTopic.isEmpty()).isTrue();
    }
}
