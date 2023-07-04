package com.example.springbootkafkasample.service;

import com.example.springbootkafkasample.dto.TopicInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final KafkaAdmin kafkaAdmin;

    public MessageService(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    public List<TopicInfo> getTopicsWithPartitions(boolean showInternalTopics) {
        ListTopicsOptions options = new ListTopicsOptions();
        options.listInternal(showInternalTopics);

        List<TopicInfo> topicPartitionCounts = new ArrayList<>();

        Map<String, TopicDescription> topicDescriptionMap;
        try (AdminClient kafkaAdminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            try {
                topicDescriptionMap = kafkaAdmin.describeTopics(kafkaAdminClient
                        .listTopics(options)
                        .names()
                        .get(1, TimeUnit.MINUTES)
                        .toArray(String[]::new));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new KafkaException("Interrupted while getting topic listings", ie);
            } catch (TimeoutException | ExecutionException ex) {
                throw new KafkaException("Failed to obtain topic listings", ex);
            }
        }

        topicDescriptionMap.forEach((topicName, topicDescription) -> {
            int partitionCount = topicDescription.partitions().size();
            int replicationCount =
                    topicDescription.partitions().get(0).replicas().size();
            topicPartitionCounts.add(new TopicInfo(topicName, partitionCount, replicationCount));
        });

        // Sort the list by topicName
        topicPartitionCounts.sort(Comparator.comparing(TopicInfo::topicName));
        return topicPartitionCounts;
    }
}
