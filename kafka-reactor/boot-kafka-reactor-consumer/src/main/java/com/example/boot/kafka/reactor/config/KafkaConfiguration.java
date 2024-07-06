package com.example.boot.kafka.reactor.config;

import com.example.boot.kafka.reactor.util.AppConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@Configuration(proxyBeanMethods = false)
class KafkaConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfiguration.class);

    @Bean
    NewTopic helloTopic() {
        log.info("Creating helloTopic");
        return new NewTopic(AppConstants.HELLO_TOPIC, 1, (short) 1);
    }
}
