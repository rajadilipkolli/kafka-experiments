package com.example.boot.kafka.reactor;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@TestConfiguration(proxyBeanMethods = false)
class TestBootKafkaReactorConsumerApplication {

    private static final Logger log = LoggerFactory.getLogger(TestBootKafkaReactorConsumerApplication.class);

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.0-alpine"));
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer(DynamicPropertyRegistry dynamicPropertyRegistry) {
        KafkaContainer kafkaContainer =
                new KafkaContainer(DockerImageName.parse("apache/kafka").withTag("3.8.0"));
        dynamicPropertyRegistry.add("spring.kafka.bootstrapServers", kafkaContainer::getBootstrapServers);
        return kafkaContainer;
    }

    @Bean
    KafkaSender<Integer, MessageDTO> reactiveKafkaSender(KafkaProperties properties) {
        log.info("Creating Sender");
        Map<String, Object> props = properties.buildProducerProperties(null);
        return KafkaSender.create(SenderOptions.create(props));
    }

    public static void main(String[] args) {
        SpringApplication.from(BootKafkaReactorConsumerApplication::main)
                .with(TestBootKafkaReactorConsumerApplication.class)
                .run(args);
    }
}
