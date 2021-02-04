package com.example.analytics.controller;

import com.example.analytics.configuration.AnalyticsConsumerConstants;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public record CountRestController(
		InteractiveQueryService interactiveQueryService) {

	@GetMapping("/counts")
	public Map<String, Long> counts() {
		Map<String, Long> counts = new HashMap<>();
		ReadOnlyKeyValueStore<String, Long> queryableStoreType = this.interactiveQueryService
				.getQueryableStore(AnalyticsConsumerConstants.PAGE_COUNT_MV, QueryableStoreTypes.keyValueStore());
		KeyValueIterator<String, Long> all = queryableStoreType.all();
		while (all.hasNext()) {
			KeyValue<String, Long> value = all.next();
			counts.put(value.key, value.value);
		}
		return counts;
	}
}
