package com.example.outboxpattern.config;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Application settings bound from the {@code application} property prefix.
 *
 * @param orderCreatedKafkaTopic Kafka topic for order-created events
 * @param cors cross-origin request settings, using defaults when omitted
 */
@ConfigurationProperties("application")
public record ApplicationProperties(String orderCreatedKafkaTopic, @DefaultValue @Valid Cors cors) {
    /**
     * Cross-origin request settings applied by the MVC configuration.
     *
     * @param pathPattern URL pattern to configure; defaults to {@code /api/**}
     * @param allowedMethods comma-separated HTTP methods; defaults to {@code *}
     * @param allowedHeaders comma-separated request headers; defaults to {@code *}
     * @param allowedOriginPatterns comma-separated origin patterns; defaults to {@code *}
     * @param allowCredentials whether credentialed requests are allowed; defaults to {@code true}
     */
    public record Cors(
            @DefaultValue("/api/**") String pathPattern,
            @DefaultValue("*") String allowedMethods,
            @DefaultValue("*") String allowedHeaders,
            @DefaultValue("*") String allowedOriginPatterns,
            @DefaultValue("true") boolean allowCredentials) {}
}
