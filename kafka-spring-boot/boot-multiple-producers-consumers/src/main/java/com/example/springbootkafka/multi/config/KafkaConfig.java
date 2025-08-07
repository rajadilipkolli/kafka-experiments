package com.example.springbootkafka.multi.config;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import com.example.springbootkafka.multi.util.AppConstants;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.RoutingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaConfig implements KafkaListenerConfigurer {

    private final KafkaProperties kafkaProperties;
    private final LocalValidatorFactoryBean validator;

    public KafkaConfig(KafkaProperties properties, LocalValidatorFactoryBean validator) {
        this.kafkaProperties = properties;
        this.validator = validator;
    }

    @Bean
    RoutingKafkaTemplate routingTemplate(GenericApplicationContext context, ProducerFactory<Object, Object> pf) {

        // Clone the PF with a different Serializer, register with Spring for shutdown
        Map<String, Object> configs = new HashMap<>(pf.getConfigurationProperties());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        DefaultKafkaProducerFactory<Object, Object> jsonPF = new DefaultKafkaProducerFactory<>(configs);
        context.registerBean("jsonPF", DefaultKafkaProducerFactory.class, () -> jsonPF);

        Map<Pattern, ProducerFactory<Object, Object>> map = new LinkedHashMap<>();
        map.put(Pattern.compile(AppConstants.TOPIC_TEST_2), jsonPF);
        map.put(Pattern.compile(".+"), pf); // Default PF with IntegerSerializer
        return new RoutingKafkaTemplate(map);
    }

    @Bean
    ConsumerFactory<Integer, String> simpleKafkaConsumerFactory() {
        Map<String, Object> consumerProperties = this.kafkaProperties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

    @Bean("simpleKafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<Integer, String> simpleKafkaListenerContainerFactory(
            ConsumerFactory<Integer, String> simpleKafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setObservationEnabled(true);
        factory.setConsumerFactory(simpleKafkaConsumerFactory);
        return factory;
    }

    // Second consumer config
    @Bean
    ConsumerFactory<String, SimpleMessage> jsonKafkaConsumerFactory() {
        Map<String, Object> consumerProperties = this.kafkaProperties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        consumerProperties.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.springbootkafka.multi.domain");
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

    @Bean("jsonKafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<String, SimpleMessage> jsonKafkaListenerContainerFactory(
            ConsumerFactory<String, SimpleMessage> jsonKafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, SimpleMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setObservationEnabled(true);
        factory.setConsumerFactory(jsonKafkaConsumerFactory);
        return factory;
    }

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(this.validator);
    }
}
