package com.example.springbootkafkasample.service;

import com.example.springbootkafkasample.dto.KafkaListenerRequest;
import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.dto.Operation;
import com.example.springbootkafkasample.dto.TopicInfo;
import com.example.springbootkafkasample.service.sender.Sender;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final KafkaAdmin kafkaAdmin;
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private final Sender sender;

    public MessageService(
            KafkaAdmin kafkaAdmin, KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry, Sender sender) {
        this.kafkaAdmin = kafkaAdmin;
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
        this.sender = sender;
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
                    topicDescription.partitions().getFirst().replicas().size();
            topicPartitionCounts.add(new TopicInfo(topicName, partitionCount, replicationCount));
        });

        // Sort the list by topicName
        topicPartitionCounts.sort(Comparator.comparing(TopicInfo::topicName));
        return topicPartitionCounts;
    }

    public Map<String, Boolean> getListenersState() {
        return kafkaListenerEndpointRegistry.getListenerContainers().stream()
                .collect(
                        Collectors.toMap(MessageListenerContainer::getListenerId, MessageListenerContainer::isRunning));
    }

    public Map<String, Boolean> updateListenerState(KafkaListenerRequest kafkaListenerRequest) {
        MessageListenerContainer listenerContainer =
                kafkaListenerEndpointRegistry.getListenerContainer(kafkaListenerRequest.containerId());
        if (listenerContainer == null) {
            throw new IllegalArgumentException(
                    "Listener container with ID '" + kafkaListenerRequest.containerId() + "' not found");
        }
        if (kafkaListenerRequest.operation().equals(Operation.START)) {
            if (!listenerContainer.isRunning()) {
                listenerContainer.start();
            }
        } else if (kafkaListenerRequest.operation().equals(Operation.STOP)) {
            if (listenerContainer.isRunning()) {
                listenerContainer.stop();
            }
        }
        return getListenersState();
    }

    public void send(MessageDTO messageDTO) {
        sender.send(messageDTO);
    }
}
