package com.example.outboxpattern;

import com.example.outboxpattern.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        ApplicationModules applicationModules = ApplicationModules.of(Application.class);

        applicationModules.verify();
    }
}
