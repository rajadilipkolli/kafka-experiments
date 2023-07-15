package com.example.boot.kafka.reactor.config;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.util.AppConstants;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@EnableKafka
@Configuration(proxyBeanMethods = false)
@Slf4j
public class KafkaConfiguration {

    @Bean
    NewTopic helloTopic() {
        log.info("Creating helloTopic");
        return new NewTopic(AppConstants.HELLO_TOPIC, 1, (short) 1);
    }

    @Bean
    KafkaSender<Integer, MessageDTO> reactiveKafkaSender(KafkaProperties properties) {
        log.info("Creating Sender");
        Map<String, Object> props = properties.buildProducerProperties();
        return KafkaSender.create(SenderOptions.create(props));
    }
}
