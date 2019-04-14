package com.example.analytics.source;

import com.example.analytics.binding.AnalyticsBinding;
import com.example.analytics.model.PageViewEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PageViewEventSource implements ApplicationRunner {

  private MessageChannel pageViewsOut;

  public PageViewEventSource(AnalyticsBinding binding) {
    this.pageViewsOut = binding.pageViewsOut();
  }

  //Sending Messages
  @Override
  public void run(ApplicationArguments args) throws Exception {
    List<String> names = Arrays.asList("Raja", "Dilip", "Chowdary", "Kolli");
    List<String> pages = Arrays.asList("blog", "sitemap", "initilizar", "news");
    Runnable runnable = () -> {
      String rPage = pages.get(new Random().nextInt(pages.size()));
      String rName = pages.get(new Random().nextInt(names.size()));
      PageViewEvent pageViewEvent = new PageViewEvent(rName, rPage, Math.random() > 5 ? 10 : 1000);
      Message<PageViewEvent> message = MessageBuilder.withPayload(pageViewEvent).setHeader(KafkaHeaders.MESSAGE_KEY, pageViewEvent.getUserId().getBytes()).build();
      try {
        this.pageViewsOut.send(message);
        log.info("Sent Message :{}", message.toString());
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    };

    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS);
  }
}
