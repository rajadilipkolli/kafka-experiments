package com.example.boot.kafka.reactor.config;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import java.util.Map;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@EnableKafka
@Configuration(proxyBeanMethods = false)
public class KafkaConfiguration {

    @Bean
    public KafkaSender<Integer, MessageDTO> reactiveKafkaSender(KafkaProperties properties) {
        Map<String, Object> props = properties.buildProducerProperties();
        return KafkaSender.create(SenderOptions.create(props));
    }
}
