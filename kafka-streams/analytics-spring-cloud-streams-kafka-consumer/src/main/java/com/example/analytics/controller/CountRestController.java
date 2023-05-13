/* Licensed under Apache-2.0 2019-2022 */
package com.example.analytics.controller;

import com.example.analytics.configuration.AnalyticsConsumerConstants;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CountRestController {

    private final InteractiveQueryService interactiveQueryService;

    private static final Logger log = LoggerFactory.getLogger(CountRestController.class);

    public CountRestController(InteractiveQueryService interactiveQueryService) {
        this.interactiveQueryService = interactiveQueryService;
    }

    @GetMapping("/counts")
    public Map<String, Long> counts() {
        Map<String, Long> counts = new HashMap<>();
        ReadOnlyKeyValueStore<String, Long> queryableStoreType =
                this.interactiveQueryService.getQueryableStore(
                        AnalyticsConsumerConstants.PAGE_COUNT_MV,
                        QueryableStoreTypes.keyValueStore());
        log.info(
                "interactiveQueryService.getCurrentHostInfo() => {}",
                interactiveQueryService.getCurrentHostInfo());
        KeyValueIterator<String, Long> all = queryableStoreType.all();
        while (all.hasNext()) {
            KeyValue<String, Long> value = all.next();
            counts.put(value.key, value.value);
        }
        return counts;
    }
}
