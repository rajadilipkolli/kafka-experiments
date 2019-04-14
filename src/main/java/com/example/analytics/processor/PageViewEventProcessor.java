package com.example.analytics.processor;

import com.example.analytics.binding.AnalyticsBinding;
import com.example.analytics.model.PageViewEvent;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class PageViewEventProcessor {

  @StreamListener
  @SendTo(AnalyticsBinding.PAGE_COUNT_OUT)
  public KStream<String, Long> processInput(@Input(AnalyticsBinding.PAGE_VIEW_IN) KStream<String, PageViewEvent> eventKStream) {

    return eventKStream
        .filter((key, value) -> value.getDuration() > 10)
        .map((key, value) -> new KeyValue<>(value.getPage(), "0"))
        .groupByKey()
//        .windowedBy(TimeWindows.of(1000 * 60))
        .count(Materialized.as(AnalyticsBinding.PAGE_COUNT_MV))
        .toStream();

    //leftJoin
//    KStream<String, Date> stringDateKStream = eventKStream.leftJoin(kTable, new ValueJoiner<PageViewEvent, Long, Date>() {
//      @Override
//      public Date apply(PageViewEvent pageViewEvent, Long aLong) {
//        return null;
//      }
//    });
  }
}
