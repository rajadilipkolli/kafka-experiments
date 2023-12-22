package com.example.outboxpattern.config;

import com.example.outboxpattern.order.OrderResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;

@Configuration
@RequiredArgsConstructor
public class ExternalizationConfiguration {

    private final GenericApplicationContext applicationContext;
    private final ApplicationProperties applicationProperties;

    EventExternalizationConfiguration eventExternalizationConfiguration() {

        return EventExternalizationConfiguration.externalizing()
                .select(EventExternalizationConfiguration.annotatedAsExternalized())
                .route(OrderResponse.class, orderCreated -> RoutingTarget.forTarget(
                                applicationProperties.getOrderCreatedKafkaTopic())
                        .andKey(String.valueOf(orderCreated.id())))
                .build();
    }

    /**
     * The {@code EventExternalizationConfiguration} bean is registered manually because the topic
     * name {@code orderCreatedKafkaTopic} is not injected at the time of automatic bean
     * registration.
     */
    @PostConstruct
    public void init() {

        this.applicationContext.registerBean(
                EventExternalizationConfiguration.class,
                this::eventExternalizationConfiguration,
                bd -> bd.setPrimary(true));
    }
}
