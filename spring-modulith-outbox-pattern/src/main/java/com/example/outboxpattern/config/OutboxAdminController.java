package com.example.outboxpattern.config;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/outbox")
public class OutboxAdminController {

    private final EventPublicationRegistry registry;

    public OutboxAdminController(EventPublicationRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        // Get the count by collecting to a list and counting
        long incomplete = registry.findIncompletePublications().size();

        // We need to filter publications by date manually
        Duration duration = Duration.ofDays(-1);
        long failed = registry.findIncompletePublicationsOlderThan(duration).size();

        return ResponseEntity.ok(Map.of(
                "pendingCount", incomplete,
                "failedCount", failed,
                "timestamp", Instant.now()));
    }
}
