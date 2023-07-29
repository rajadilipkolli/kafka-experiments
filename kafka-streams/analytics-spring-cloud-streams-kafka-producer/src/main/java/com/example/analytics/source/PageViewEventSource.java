/* Licensed under Apache-2.0 2019-2023 */
package com.example.analytics.source;

import com.example.analytics.model.PageViewEvent;
import java.security.SecureRandom;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PageViewEventSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageViewEventSource.class);

    @Bean
    public Supplier<PageViewEvent> pageViewEventSupplier() {
        List<String> names = List.of("rajesh", "kumar", "raja", "dilip", "chowdary", "kolli");
        List<String> pages = List.of("blog", "sitemap", "initializr", "news", "colophon", "about");
        return () -> {
            String rPage = pages.get(new SecureRandom().nextInt(pages.size()));
            String rName = names.get(new SecureRandom().nextInt(names.size()));
            PageViewEvent pageViewEvent =
                    new PageViewEvent(rName, rPage, new SecureRandom().nextInt(10) > 5 ? 10 : 1000);
            LOGGER.info("Publishing Event :{}", pageViewEvent);
            return pageViewEvent;
        };
    }
}
