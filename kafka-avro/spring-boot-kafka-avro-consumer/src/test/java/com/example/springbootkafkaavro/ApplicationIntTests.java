package com.example.springbootkafkaavro;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.springbootkafkaavro.common.KafkaContainersConfig;
import com.example.springbootkafkaavro.entity.PersonEntity;
import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.repository.PersonRepository;
import com.example.springbootkafkaavro.util.ApplicationConstants;
import java.time.Duration;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({KafkaContainersConfig.class, KafkaProducer.class})
@ActiveProfiles("test")
class ApplicationIntTests {

    @Autowired private PersonRepository personRepository;
    @Autowired private KafkaProducer kafkaProducer;
    @Autowired private KafkaTemplate<String, Person> kafkaTemplate;

    @Test
    void contextLoads() {
        // Get initial count to account for previous tests
        long initialCount = personRepository.count();

        Person person = new Person();
        person.setAge(33);
        person.setName("junit");
        this.kafkaProducer.sendMessage(person);
        await().atMost(10, SECONDS)
                .untilAsserted(
                        () -> assertThat(personRepository.count()).isEqualTo(initialCount + 1));
    }

    @Test
    void shouldDemonstrateSchemaEvolution() {
        // Clear any existing data
        personRepository.deleteAll();

        // Test 1: Send a message with only basic fields (simulating V1 message)
        Person personV1 = new Person();
        personV1.setId(1L);
        personV1.setName("John Doe");
        personV1.setAge(30);
        personV1.setGender("Male");
        // Note: email and phoneNumber are not set (simulating V1 schema)

        ProducerRecord<String, Person> recordV1 =
                new ProducerRecord<>(ApplicationConstants.PERSONS_TOPIC, "john", personV1);
        kafkaTemplate.send(recordV1);

        // Test 2: Send a message with all fields (V2 message)
        Person personV2 = new Person();
        personV2.setId(2L);
        personV2.setName("Jane Smith");
        personV2.setAge(25);
        personV2.setGender("Female");
        personV2.setEmail("jane.smith@example.com");
        personV2.setPhoneNumber("+1-555-0123");

        ProducerRecord<String, Person> recordV2 =
                new ProducerRecord<>(ApplicationConstants.PERSONS_TOPIC, "jane", personV2);
        kafkaTemplate.send(recordV2);

        // Wait for messages to be processed
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(
                        () -> {
                            assertThat(personRepository.count()).isEqualTo(2);
                        });

        // Verify that both messages were processed correctly
        var persons = personRepository.findAll();
        assertThat(persons).hasSize(2);

        // Verify V1 message (basic fields only)
        PersonEntity johnEntity =
                persons.stream()
                        .filter(p -> "John Doe".equals(p.getName()))
                        .findFirst()
                        .orElseThrow();
        assertThat(johnEntity.getName()).isEqualTo("John Doe");
        assertThat(johnEntity.getAge()).isEqualTo(30);
        assertThat(johnEntity.getGender()).isEqualTo("Male");
        assertThat(johnEntity.getEmail()).isNull(); // Should be null for V1 message
        assertThat(johnEntity.getPhoneNumber()).isNull(); // Should be null for V1 message

        // Verify V2 message (all fields)
        PersonEntity janeEntity =
                persons.stream()
                        .filter(p -> "Jane Smith".equals(p.getName()))
                        .findFirst()
                        .orElseThrow();
        assertThat(janeEntity.getName()).isEqualTo("Jane Smith");
        assertThat(janeEntity.getAge()).isEqualTo(25);
        assertThat(janeEntity.getGender()).isEqualTo("Female");
        assertThat(janeEntity.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(janeEntity.getPhoneNumber()).isEqualTo("+1-555-0123");

        System.out.println("=== SCHEMA EVOLUTION TEST RESULTS ===");
        System.out.println("Successfully processed both V1 and V2 messages:");
        System.out.println(
                "V1 message: " + johnEntity.getName() + " (email: " + johnEntity.getEmail() + ")");
        System.out.println(
                "V2 message: " + janeEntity.getName() + " (email: " + janeEntity.getEmail() + ")");
        System.out.println("=== TEST COMPLETED SUCCESSFULLY ===");
    }
}
