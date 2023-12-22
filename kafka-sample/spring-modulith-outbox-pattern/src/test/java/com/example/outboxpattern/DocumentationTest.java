package com.example.outboxpattern;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class DocumentationTest {

    @Test
    void createModulithsDocumentation() {

        new Documenter(ApplicationModules.of(Application.class)).writeDocumentation();
    }
}
