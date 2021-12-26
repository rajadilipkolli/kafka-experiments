package com.sivalabs.springbootkafkaavro;

import com.sivalabs.springbootkafkaavro.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class SpringBootKafkaAvroApplicationTests {

  @Autowired PersonRepository personRepository;

  @Test
  void contextLoads() {
    await()
        .atMost(10, SECONDS)
        .untilAsserted(() -> assertThat(personRepository.count()).isEqualTo(1));
  }
}
