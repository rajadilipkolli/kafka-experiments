package com.example.analytics.sink;

import java.util.function.Consumer;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class PageCountSink {

    private static final Logger log = LoggerFactory.getLogger(PageCountSink.class);

    @Bean
    public Consumer<KTable<String, Long>> processStreamFromPcsTopic() {
        return kTableCounts ->
                kTableCounts
                        .toStream()
                        .foreach((key, value) -> log.info("Key = {}, value = {}", key, value));
    }
}
