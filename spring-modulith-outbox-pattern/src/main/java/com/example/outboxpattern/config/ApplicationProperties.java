package com.example.outboxpattern.config;

import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Application settings bound from the {@code application} property prefix.
 *
 * @param orderCreatedKafkaTopic Kafka topic for order-created events
 * @param cors cross-origin request settings, using defaults when omitted
 * @param maxPageSize maximum allowed page size for queries
 * @param maxItemCount maximum items per order request
 * @param resubmissionBatchSize batch size for outbox resubmission
 * @param resubmissionMaxInFlight max in-flight events for outbox resubmission
 * @param resubmissionMinAge minimum age for outbox resubmission
 */
@ConfigurationProperties("application")
public record ApplicationProperties(
        String orderCreatedKafkaTopic,
        @DefaultValue @Valid Cors cors,
        @DefaultValue("50") int maxPageSize,
        @DefaultValue("100") int maxItemCount,
        @DefaultValue("100") int resubmissionBatchSize,
        @DefaultValue("50") int resubmissionMaxInFlight,
        @DefaultValue("5m") Duration resubmissionMinAge) {
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
