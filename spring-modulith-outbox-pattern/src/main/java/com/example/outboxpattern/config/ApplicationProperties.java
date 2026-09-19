package com.example.outboxpattern.config;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("application")
public class ApplicationProperties {

    private String orderCreatedKafkaTopic;

    @NestedConfigurationProperty
    @Valid private Cors cors = new Cors();

    public ApplicationProperties() {}

    public String getOrderCreatedKafkaTopic() {
        return this.orderCreatedKafkaTopic;
    }

    public Cors getCors() {
        return this.cors;
    }

    public void setOrderCreatedKafkaTopic(final String orderCreatedKafkaTopic) {
        this.orderCreatedKafkaTopic = orderCreatedKafkaTopic;
    }

    public void setCors(final Cors cors) {
        this.cors = cors;
    }

    public static class Cors {
        private String pathPattern = "/api/**";
        private String allowedMethods = "*";
        private String allowedHeaders = "*";
        private String allowedOriginPatterns = "*";
        private boolean allowCredentials = true;

        public Cors() {}

        public String getPathPattern() {
            return this.pathPattern;
        }

        public String getAllowedMethods() {
            return this.allowedMethods;
        }

        public String getAllowedHeaders() {
            return this.allowedHeaders;
        }

        public String getAllowedOriginPatterns() {
            return this.allowedOriginPatterns;
        }

        public boolean isAllowCredentials() {
            return this.allowCredentials;
        }

        public void setPathPattern(final String pathPattern) {
            this.pathPattern = pathPattern;
        }

        public void setAllowedMethods(final String allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public void setAllowedHeaders(final String allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public void setAllowedOriginPatterns(final String allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns;
        }

        public void setAllowCredentials(final boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }
    }
}
