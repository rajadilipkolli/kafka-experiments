package com.example.springbootkafkasample.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.admin.*;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final KafkaAdmin kafkaAdmin;

    public MessageService(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    public Map<String, Integer> getTopicsWithPartitions(boolean showInternalTopics)
            throws ExecutionException, InterruptedException, TimeoutException {
        ListTopicsOptions options = new ListTopicsOptions();
        options.listInternal(showInternalTopics);

        Map<String, Integer> topicPartitionCounts = new HashMap<>();

        Map<String, TopicDescription> topicDescriptionMap;
        try (Admin kafkaAdminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            topicDescriptionMap = kafkaAdminClient
                    .describeTopics(kafkaAdminClient.listTopics(options).names().get(1, TimeUnit.MINUTES))
                    .allTopicNames()
                    .get(1, TimeUnit.MINUTES);
        }

        topicDescriptionMap.forEach((topicName, topicDescription) -> {
            int partitionCount = topicDescription.partitions().size();
            topicPartitionCounts.put(topicName, partitionCount);
        });

        return topicPartitionCounts;
    }
}
