package com.example.analytics.binding;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface AnalyticsBinding {

  String PAGE_VIEW_OUT = "pvout";

  //Page Views
  @Output(PAGE_VIEW_OUT)
  MessageChannel pageViewsOut();
}
