package com.example.springbootkafkasample.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@EnableKafka
public class KafkaConfig implements KafkaListenerConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfig.class);

    private final LocalValidatorFactoryBean validator;

    public KafkaConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(this.validator);
    }

    @Bean
    KafkaListenerErrorHandler validationErrorHandler() {
        return (m, e) -> {
            LOGGER.error("Error for message :{} ", m, e);
            return m;
        };
    }
}
