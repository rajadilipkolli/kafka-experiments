package com.example.analytics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
    + "org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration")
public class AnalyticsApplicationTests {

  @Test
  public void contextLoads() {
  }

}
