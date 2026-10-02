package com.example.springbootkafkaavro.repository;

import com.example.springbootkafkaavro.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long> {
    /**
     * Checks whether a person event with the identifier has already been persisted.
     *
     * @param eventId the event identifier to look up
     * @return whether the event exists
     */
    boolean existsByEventId(java.util.UUID eventId);
}
