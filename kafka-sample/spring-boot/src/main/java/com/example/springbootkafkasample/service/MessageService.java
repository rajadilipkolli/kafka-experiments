package com.example.springbootkafkasample.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final KafkaAdminClient kafkaAdminClient;

    public MessageService(KafkaAdminClient kafkaAdminClient) {
        this.kafkaAdminClient = kafkaAdminClient;
    }

    public Map<String, Integer> getTopicsWithPartitions(boolean showInternalTopics)
            throws ExecutionException, InterruptedException, TimeoutException {
        ListTopicsOptions options = new ListTopicsOptions();
        options.listInternal(showInternalTopics);

        Map<String, Integer> topicPartitionCounts = new HashMap<>();

        Map<String, TopicDescription> topicDescriptionMap = kafkaAdminClient
                .describeTopics(kafkaAdminClient.listTopics(options).names().get(1, TimeUnit.MINUTES))
                .allTopicNames()
                .get(1, TimeUnit.MINUTES);

        topicDescriptionMap.forEach((topicName, topicDescription) -> {
            int partitionCount = topicDescription.partitions().size();
            topicPartitionCounts.put(topicName, partitionCount);
        });

        return topicPartitionCounts;
    }
}
