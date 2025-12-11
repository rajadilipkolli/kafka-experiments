package com.example.quarkuskafkademo;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaTestResource implements QuarkusTestResourceLifecycleManager {

    private KafkaContainer kafka;

    @Override
    public Map<String, String> start() {
        kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("4.1.1"));
        kafka.start();

        Map<String, String> config = new HashMap<>();
        config.put("mp.messaging.connector.smallrye-kafka.bootstrap.servers", kafka.getBootstrapServers());
        // Disable Quarkus DevServices for Kafka to prevent Quarkus from
        // starting its own broker (it may start Redpanda). We explicitly
        // start the KafkaContainer we want (apache/kafka-native).
        config.put("quarkus.kafka.devservices.enabled", "false");
        // Also disable global devservices as a fallback
        config.put("quarkus.devservices.enabled", "false");
        return config;
    }

    @Override
    public void stop() {
        if (kafka != null) {
            kafka.stop();
        }
    }
}
