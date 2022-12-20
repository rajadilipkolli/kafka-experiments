package com.sivalabs.springbootkafka.multi.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.sivalabs.springbootkafka.multi.domain.SimpleMessage;

import java.util.Map;

@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaConfig {

    @Autowired
    private KafkaProperties properties;

    @Autowired
    private ObjectProvider<RecordMessageConverter> messageConverterProvider;

    @Bean
    public KafkaTemplate<Integer, String> simpleKafkaTemplate(ProducerFactory<Integer, String> simpleProducerFactory) {
        KafkaTemplate<Integer, String> kafkaTemplate = new KafkaTemplate<>(simpleProducerFactory);
        RecordMessageConverter messageConverter = messageConverterProvider.getIfUnique();
        if (messageConverter != null) {
            kafkaTemplate.setMessageConverter(messageConverter);
        }
        return kafkaTemplate;
    }

    @Bean
    public ProducerFactory<Integer, String> simpleProducerFactory() {
        Map<String, Object> producerProperties = this.properties.buildProducerProperties();
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        DefaultKafkaProducerFactory<Integer, String> factory = new DefaultKafkaProducerFactory<>(producerProperties);
        String transactionIdPrefix = this.properties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        return factory;
    }

    @Bean
    public ConsumerFactory<Integer, String> simpleKafkaConsumerFactory() {
        Map<String, Object> consumerProperties = this.properties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }


    @Bean("simpleKafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<Integer, String> simpleKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(simpleKafkaConsumerFactory());
        return factory;
    }

    // Second consumer/producer config

    @Bean
    public KafkaTemplate<String, SimpleMessage> jsonKafkaTemplate(ProducerFactory<String, SimpleMessage> jsonProducerFactory) {
        KafkaTemplate<String, SimpleMessage> kafkaTemplate = new KafkaTemplate<>(jsonProducerFactory);
        RecordMessageConverter messageConverter = messageConverterProvider.getIfUnique();
        if (messageConverter != null) {
            kafkaTemplate.setMessageConverter(messageConverter);
        }
        return kafkaTemplate;
    }

    @Bean
    public ProducerFactory<String, SimpleMessage> jsonProducerFactory() {
        Map<String, Object> producerProperties = this.properties.buildProducerProperties();
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        DefaultKafkaProducerFactory<String, SimpleMessage> factory = new DefaultKafkaProducerFactory<>(producerProperties);
        String transactionIdPrefix = this.properties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        return factory;
    }

    @Bean
    public ConsumerFactory<String, SimpleMessage> jsonKafkaConsumerFactory() {
        Map<String, Object> consumerProperties = this.properties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        consumerProperties.put(JsonDeserializer.TRUSTED_PACKAGES,"com.sivalabs.springbootkafka.multi.domain");
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }


    @Bean("jsonKafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<String, SimpleMessage> jsonKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SimpleMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(jsonKafkaConsumerFactory());
        return factory;
    }
}
