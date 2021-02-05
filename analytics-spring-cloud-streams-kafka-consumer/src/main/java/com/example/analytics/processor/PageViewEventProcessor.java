package com.example.analytics.processor;

import com.example.analytics.configuration.AnalyticsConsumerConstants;
import com.example.analytics.model.PageViewEvent;
import java.util.function.Function;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class PageViewEventProcessor {

  @Bean
  public Function<KStream<String, PageViewEvent>, KStream<String, Long>> processInput() {

    return eventKStream ->
        eventKStream
            .filter((key, value) -> value.getDuration() > 10)
            .map((key, value) -> new KeyValue<>(value.getPage(), "0"))
            .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
            //        .windowedBy(TimeWindows.of(1000 * 60))
            .count(
                Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(
                        AnalyticsConsumerConstants.PAGE_COUNT_MV)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(Serdes.Long()))
            .toStream();

    // leftJoin
    //    KStream<String, Date> stringDateKStream = eventKStream.leftJoin(kTable, new
    // ValueJoiner<PageViewEvent, Long, Date>() {
    //      @Override
    //      public Date apply(PageViewEvent pageViewEvent, Long aLong) {
    //        return null;
    //      }
    //    });
  }
}
