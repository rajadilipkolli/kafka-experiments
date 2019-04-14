package com.example.analytics.binding;

import com.example.analytics.model.PageViewEvent;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface AnalyticsBinding {

  String PAGE_VIEW_OUT = "pvout";
  String PAGE_VIEW_IN = "pvin";
  String PAGE_COUNT_MV = "pcmv";
  String PAGE_COUNT_OUT = "pcout";
  String PAGE_COUNT_IN = "pcin";

  //Page Views
  @Input(PAGE_VIEW_IN)
  KStream<String, PageViewEvent> pageViewsIn();

  @Output(PAGE_VIEW_OUT)
  MessageChannel pageViewsOut();

  //Page Count
  @Output(PAGE_COUNT_OUT)
  KStream<String, PageViewEvent> pageCountOut();

  @Input(PAGE_COUNT_IN)
  KTable<String, Long> pageCountIn();
}
