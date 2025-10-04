/* Licensed under Apache-2.0 2025 */
package com.example.analytics.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = ContainersConfiguration.class)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired protected KafkaTemplate<String, String> kafkaTemplate;

    @Autowired protected MockMvc mockMvc;

    @Autowired protected ObjectMapper objectMapper;
}
