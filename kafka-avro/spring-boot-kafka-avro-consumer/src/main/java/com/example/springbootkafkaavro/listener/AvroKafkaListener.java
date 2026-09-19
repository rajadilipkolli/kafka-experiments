package com.example.springbootkafkaavro.listener;

import com.example.springbootkafkaavro.entity.PersonEntity;
import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.repository.PersonRepository;
import com.example.springbootkafkaavro.util.ApplicationConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AvroKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(AvroKafkaListener.class);
    private final PersonRepository personRepository;

    public AvroKafkaListener(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @KafkaListener(topics = ApplicationConstants.PERSONS_TOPIC, groupId = "avro-group")
    public void handler(ConsumerRecord<String, Person> personConsumerRecord) {
        Person person = personConsumerRecord.value();

        // Log schema evolution demonstration
        String schemaInfo = determineSchemaVersion(person);
        log.info("=== SCHEMA EVOLUTION DEMO ===");
        log.info("Received Person with {}", schemaInfo);
        log.info(
                "Person received - Name: {}, Age: {}, Gender: {}",
                person.getName(),
                person.getAge(),
                person.getGender());

        // Handle new fields from version 2 (these will be null for version 1 messages)
        String email = getFieldValue(person, "email");
        String phoneNumber = getFieldValue(person, "phoneNumber");

        if (email != null || phoneNumber != null) {
            log.info("V2 fields detected - Email: {}, Phone: {}", email, phoneNumber);
        } else {
            log.info("V1 message - No email/phone fields available (backward compatibility)");
        }

        PersonEntity personEntity =
                new PersonEntity()
                        .setName(person.getName().toString())
                        .setAge(person.getAge())
                        .setGender(
                                person.getGender() != null ? person.getGender().toString() : null)
                        .setEmail(email)
                        .setPhoneNumber(phoneNumber);

        PersonEntity savedEntity = this.personRepository.save(personEntity);
        log.info("Person saved to database with ID: {}", savedEntity.getId());
        log.info("=== END SCHEMA EVOLUTION DEMO ===");
    }

    private String determineSchemaVersion(Person person) {
        org.apache.avro.Schema schema = person.getSchema();
        if (hasField(schema, "email") && hasField(schema, "phoneNumber")) {
            return "Version 2 schema (includes email and phoneNumber fields)";
        } else {
            return "Version 1 schema (basic fields only)";
        }
    }

    private boolean hasField(org.apache.avro.Schema schema, String fieldName) {
        return schema.getField(fieldName) != null;
    }

    private String getFieldValue(Person person, String fieldName) {
        try {
            Object value = person.get(fieldName);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            // Field doesn't exist in this schema version
            return null;
        }
    }
}
