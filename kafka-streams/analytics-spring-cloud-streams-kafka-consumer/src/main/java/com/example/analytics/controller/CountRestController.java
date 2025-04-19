/* Licensed under Apache-2.0 2019-2023 */
package com.example.analytics.controller;

import com.example.analytics.configuration.AnalyticsConsumerConstants;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CountRestController {

    private final InteractiveQueryService interactiveQueryService;

    private static final Logger log = LoggerFactory.getLogger(CountRestController.class);

    public CountRestController(InteractiveQueryService interactiveQueryService) {
        this.interactiveQueryService = interactiveQueryService;
    }

    @GetMapping("/api/page-counts")
    public Map<String, Long> counts() {
        Map<String, Long> counts = new HashMap<>();
        ReadOnlyKeyValueStore<String, Long> queryableStoreType =
                this.interactiveQueryService.getQueryableStore(
                        AnalyticsConsumerConstants.PAGE_COUNT_MV,
                        QueryableStoreTypes.keyValueStore());
        log.info(
                "interactiveQueryService.getCurrentHostInfo() => {}",
                interactiveQueryService.getCurrentHostInfo());
        queryableStoreType.all().forEachRemaining(kv -> counts.put(kv.key, kv.value));

        return counts;
    }
}
