package com.example.kafka.common;

import com.example.kafka.listener.AppListener;
import com.example.kafka.processor.ToUpperStringProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.lang.NonNull;

@TestConfiguration(proxyBeanMethods = false)
public class ProducerConfig {

    @Bean("toUpperStringProcessors")
    List<ToUpperStringProcessor> toUpperStringProcessors() {
        return Collections.synchronizedList(new ArrayList<>());
    }

    @Bean
    BeanPostProcessor beanPostProcessor(
            @Qualifier("toUpperStringProcessors") List<ToUpperStringProcessor> toUpperStringProcessors) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName)
                    throws BeansException {
                if (bean instanceof ToUpperStringProcessor toUpperStringProcessor) {
                    toUpperStringProcessors.add(toUpperStringProcessor);
                }
                return bean;
            }
        };
    }

    @Bean
    NewTopic springKafkaTestTopic() {
        return TopicBuilder.name(AppListener.SPRING_KAFKA_TEST_TOPIC)
                .partitions(5)
                .build();
    }

    @Bean
    Function<MessageListenerContainer, String> threadNameSupplier() {
        return messageListenerContainer -> messageListenerContainer.getGroupId() + "-"
                + List.of(messageListenerContainer.getListenerId().split("-")).getLast();
    }
}
