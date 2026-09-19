package com.example.boot.kafka.reactor.config;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.util.AppConstants;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@EnableKafka
@Configuration(proxyBeanMethods = false)
class KafkaConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfiguration.class);

    @Bean
    NewTopic helloTopic() {
        log.info("Creating helloTopic");
        return new NewTopic(AppConstants.HELLO_TOPIC, 1, (short) 1);
    }

    @Bean
    KafkaSender<Integer, MessageDTO> reactiveKafkaSender(KafkaProperties properties) {
        log.info("Creating Sender");
        Map<String, Object> props = properties.buildProducerProperties();
        SenderOptions<Integer, MessageDTO> senderOptions = SenderOptions.create(props);
        return KafkaSender.create(senderOptions);
    }
}
