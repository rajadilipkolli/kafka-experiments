package com.example.analytics.sink;

import com.example.analytics.binding.AnalyticsBinding;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PageCountSink {

  @StreamListener
  public void process(@Input(AnalyticsBinding.PAGE_COUNT_IN) KTable<String, Long> kTableCounts) {
    kTableCounts
        .toStream()
        .foreach((key, value) -> log.info("Key = {}, value = {}", key, value));
  }
}
