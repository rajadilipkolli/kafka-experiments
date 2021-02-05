package com.example.analytics;

import com.example.analytics.binding.AnalyticsBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.stream.annotation.EnableBinding;

@SpringBootApplication
@EnableBinding(AnalyticsBinding.class)
@ConfigurationPropertiesScan("com.example.analytics")
public class AnalyticsProducerApplication {

  public static void main(String[] args) {
    SpringApplication.run(AnalyticsProducerApplication.class, args);
  }
}
