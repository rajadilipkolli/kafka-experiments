package com.example.outboxpattern.config;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("application")
public record ApplicationProperties(String orderCreatedKafkaTopic, @DefaultValue @Valid Cors cors) {
    public record Cors(
            @DefaultValue("/api/**") String pathPattern,
            @DefaultValue("*") String allowedMethods,
            @DefaultValue("*") String allowedHeaders,
            @DefaultValue("*") String allowedOriginPatterns,
            @DefaultValue("true") boolean allowCredentials) {}
}
