package com.example.boot.kafka.reactor.config;

import com.example.boot.kafka.reactor.util.AppConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;

@EnableKafka
@Configuration(proxyBeanMethods = false)
class KafkaConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfiguration.class);

    /** Declares the hello topic with three partitions and the broker default replication factor. */
    @Bean
    NewTopic helloTopic() {
        log.info("Creating helloTopic");
        return TopicBuilder.name(AppConstants.HELLO_TOPIC).partitions(3).build();
    }

    /**
     * Creates an observed listener factory using Spring Boot Kafka settings. Listeners returning
     * {@code Mono} are acknowledged asynchronously when their result completes.
     */
    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory) {
        // The Mono return type switches the container to MANUAL with async acks.
        // A record is acknowledged when its Mono completes.
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, consumerFactory);
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }
}
