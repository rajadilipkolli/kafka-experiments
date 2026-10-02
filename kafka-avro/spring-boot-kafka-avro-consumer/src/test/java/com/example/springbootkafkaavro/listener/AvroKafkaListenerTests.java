package com.example.springbootkafkaavro.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.springbootkafkaavro.entity.PersonEntity;
import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.repository.PersonRepository;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

class AvroKafkaListenerTests {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                "external-event-123",
                "イベント-123",
                " ",
                "123-xyz",
                "123e4567-e89b-12d3-a456-426614174000"
            })
    void acceptsSchemaEventIdsAndDeduplicatesRedelivery(String suppliedId) {
        PersonRepository repository = mock(PersonRepository.class);
        when(repository.saveAndFlush(any(PersonEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AvroKafkaListener listener = new AvroKafkaListener(repository);
        Person person = new Person();
        person.setName("test");
        person.setAge(30);
        person.setEventId(suppliedId);
        ConsumerRecord<String, Person> record =
                new ConsumerRecord<>("persons", 2, 42L, "key", person);

        listener.handler(record);

        ArgumentCaptor<PersonEntity> saved = ArgumentCaptor.forClass(PersonEntity.class);
        verify(repository).saveAndFlush(saved.capture());
        UUID expected =
                suppliedId == null
                        ? UUID.nameUUIDFromBytes("persons-2-42".getBytes(StandardCharsets.UTF_8))
                        : suppliedId.equals("123e4567-e89b-12d3-a456-426614174000")
                                ? UUID.fromString(suppliedId)
                                : UUID.nameUUIDFromBytes(
                                        suppliedId.getBytes(StandardCharsets.UTF_8));
        assertThat(saved.getValue().getEventId()).isEqualTo(expected);
        when(repository.existsByEventId(expected)).thenReturn(true);

        // A separate listener instance must derive the same ID and skip the second insert.
        new AvroKafkaListener(repository).handler(record);
        verify(repository).saveAndFlush(any(PersonEntity.class));
    }
}
