package com.example.springbootkafkaavro;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.springbootkafkaavro.common.KafkaContainersConfig;
import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        properties = {
            "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
            "spring.kafka.producer.value-serializer=io.confluent.kafka.serializers.KafkaAvroSerializer"
        },
        classes = {KafkaContainersConfig.class})
@Import(KafkaProducer.class)
class ApplicationIntTests {

    @Autowired PersonRepository personRepository;
    @Autowired KafkaProducer kafkaProducer;

    @Test
    void contextLoads() {
        Person person = new Person();
        person.setAge(33);
        person.setName("junit");
        this.kafkaProducer.sendMessage(person);
        await().atMost(10, SECONDS)
                .untilAsserted(() -> assertThat(personRepository.count()).isEqualTo(1));
    }
}
