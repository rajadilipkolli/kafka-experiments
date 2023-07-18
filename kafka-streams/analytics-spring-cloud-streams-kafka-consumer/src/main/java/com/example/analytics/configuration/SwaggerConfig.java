/* Licensed under Apache-2.0 2023 */
package com.example.analytics.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
        info = @Info(title = "kafka-streams-analytics-consumer", version = "v1"),
        servers = @Server(url = "/"))
public class SwaggerConfig {}
