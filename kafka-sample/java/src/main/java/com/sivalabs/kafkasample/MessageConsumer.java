package com.sivalabs.kafkasample;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MessageConsumer {

    private final String TOPIC_NAME;

    MessageConsumer(String topic_name) {
        TOPIC_NAME = topic_name;
    }

    public static void main(String[] args) {
        MessageConsumer consumer = new MessageConsumer("streams-namecount-output");
        consumer.run();
    }

    void run() {
        KafkaConsumer<String, Long> kafkaConsumer = getKafkaConsumer();
        //Start processing messages
        try (kafkaConsumer) {
            kafkaConsumer.subscribe(Collections.singletonList(TOPIC_NAME));
            while (true) {
                ConsumerRecords<String, Long> records = kafkaConsumer.poll(Duration.ofMillis(100));
                if (!records.isEmpty()) {
                    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
                    for (ConsumerRecord<String, Long> record : records) {
                        System.out.println("Received: " + record.key() + ":" + record.value());
                        offsets.put(new TopicPartition(record.topic(), record.partition()),
                                new OffsetAndMetadata(record.offset() + 1));
                    }
                    kafkaConsumer.commitSync(offsets);
                }
            }
        } catch (WakeupException ex) {
            System.out.println("Exception caught " + ex.getMessage());
        } finally {
            System.out.println("After closing KafkaConsumer");
        }
    }

    private KafkaConsumer<String, Long> getKafkaConsumer() {
        Properties configProperties = new Properties();
        configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "groupId");
        configProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "simple");
        configProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        //Figure out where to start processing messages from
        return new KafkaConsumer<>(configProperties);
    }
}

