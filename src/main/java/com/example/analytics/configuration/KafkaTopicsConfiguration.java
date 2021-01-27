package com.example.analytics.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicsConfiguration {

    private final AnalyticsApplicationProperties analyticsApplicationProperties;


    @Bean
    public NewTopic pvsTopic() {

        return new NewTopic(analyticsApplicationProperties.getTopicNamePvs(), analyticsApplicationProperties.getPartitions(), analyticsApplicationProperties.getReplication());
    }

    @Bean
    public NewTopic kafkaStreamspcmvRePartitionTopic() {
        return new NewTopic(analyticsApplicationProperties.getTopicNameRePartition(), analyticsApplicationProperties.getPartitions(), analyticsApplicationProperties.getReplication());
    }

    @Bean
    public NewTopic kafkaStreamspcmvChangeLogTopic() {
        return new NewTopic(analyticsApplicationProperties.getTopicNameChangelog(), analyticsApplicationProperties.getPartitions(), analyticsApplicationProperties.getReplication());
    }

    @Bean
    public NewTopic pcsTopic() {

        return new NewTopic(analyticsApplicationProperties.getTopicNamePcs(), analyticsApplicationProperties.getPartitions(), analyticsApplicationProperties.getReplication());

    }
}
