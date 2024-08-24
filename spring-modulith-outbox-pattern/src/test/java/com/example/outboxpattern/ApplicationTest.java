package com.example.outboxpattern;

import com.example.outboxpattern.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ApplicationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        ApplicationModules applicationModules = ApplicationModules.of(ModulithApplication.class);

        applicationModules.verify();
    }

    @Test
    void createModulithsDocumentation() {

        new Documenter(ApplicationModules.of(ModulithApplication.class)).writeDocumentation();
    }
}
