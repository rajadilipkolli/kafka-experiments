package com.example.analytics.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

//to use the constructor binding, we need to explicitly enable our configuration class either with @EnableConfigurationProperties or with @ConfigurationPropertiesScan.
@ConstructorBinding
@ConfigurationProperties(prefix = "io.confluent.developer.topic")
@Getter
@RequiredArgsConstructor
public class AnalyticsApplicationProperties {

    @NotBlank
    private final String topicNamePvs;

    @Positive
    private final short replication;
    
    @Positive
    private final short partitions;

}
