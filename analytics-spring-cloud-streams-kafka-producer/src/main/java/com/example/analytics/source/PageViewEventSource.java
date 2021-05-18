package com.example.analytics.source;

import com.example.analytics.binding.AnalyticsBinding;
import com.example.analytics.model.PageViewEvent;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class PageViewEventSource implements ApplicationRunner {

  private final MessageChannel pageViewsOut;

  private static final Logger log = LoggerFactory.getLogger(PageViewEventSource.class);

  public PageViewEventSource(AnalyticsBinding binding) {
    this.pageViewsOut = binding.pageViewsOut();
  }

  // Sending Messages
  @Override
  public void run(ApplicationArguments args) {
    List<String> names = List.of("Raja", "Dilip", "Chowdary", "Kolli");
    List<String> pages = List.of("blog", "sitemap", "initializer", "news");
    Runnable runnable =
        () -> {
          String rPage = pages.get(new SecureRandom().nextInt(pages.size()));
          String rName = names.get(new SecureRandom().nextInt(names.size()));
          PageViewEvent pageViewEvent =
              new PageViewEvent(rName, rPage, Math.random() > 5 ? 10 : 1000);
          Message<PageViewEvent> message =
              MessageBuilder.withPayload(pageViewEvent)
                  .setHeader(KafkaHeaders.MESSAGE_KEY, pageViewEvent.getUserId().getBytes())
                  .build();
          try {
            this.pageViewsOut.send(message);
            log.info("Sent Message :{}", message.toString());
          } catch (Exception e) {
            log.error(e.getMessage());
          }
        };

    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(runnable, 1, 1, TimeUnit.MILLISECONDS);
  }
}
