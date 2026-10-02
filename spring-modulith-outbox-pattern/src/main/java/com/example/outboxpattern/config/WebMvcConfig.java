package com.example.outboxpattern.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
class WebMvcConfig implements WebMvcConfigurer {

    private final ApplicationProperties applicationProperties;

    WebMvcConfig(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Registers CORS settings for the configured path, splitting comma-separated methods, headers, and origin patterns.
     *
     * @param registry registry to receive the CORS mapping
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        ApplicationProperties.Cors propertiesCors = applicationProperties.cors();
        registry.addMapping(propertiesCors.pathPattern())
                .allowedMethods(StringUtils.tokenizeToStringArray(propertiesCors.allowedMethods(), ","))
                .allowedHeaders(StringUtils.tokenizeToStringArray(propertiesCors.allowedHeaders(), ","))
                .allowedOriginPatterns(StringUtils.tokenizeToStringArray(propertiesCors.allowedOriginPatterns(), ","))
                .allowCredentials(propertiesCors.allowCredentials());
    }
}
