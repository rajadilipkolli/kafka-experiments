package com.example.analytics.source;

import com.example.analytics.model.PageViewEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@Component
public class PageViewEventSource {

  @Bean
  public Supplier<PageViewEvent> pageViewEventSupplier() {
    List<String> names =
        List.of("mfisher", "dyser", "schacko", "abilan", "ozhurakousky", "grussell");
    List<String> pages = List.of("blog", "sitemap", "initializr", "news", "colophon", "about");
    return () -> {
      String rPage = pages.get(new Random().nextInt(pages.size()));
      String rName = pages.get(new Random().nextInt(names.size()));
      return new PageViewEvent(rName, rPage, Math.random() > .5 ? 10 : 1000);
    };
  }
}
