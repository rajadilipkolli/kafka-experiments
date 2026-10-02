package com.example.springbootkafkaavro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "person_entity")
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    private Integer age;

    private String gender;

    private String email;

    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private UUID eventId;

    /** Creates an empty entity for JPA and fluent field initialization. */
    public PersonEntity() {}

    /** Returns the stored age. */
    public Integer getAge() {
        return age;
    }

    public PersonEntity setAge(Integer age) {
        this.age = age;
        return this;
    }

    public Long getId() {
        return id;
    }

    public PersonEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PersonEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public PersonEntity setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public PersonEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    /** Returns the optional phone number. */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the optional phone number.
     *
     * @param phoneNumber the phone number, or {@code null} when absent
     * @return this entity for chained updates
     */
    public PersonEntity setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    /** Returns the event identifier used to detect duplicate deliveries. */
    public UUID getEventId() {
        return eventId;
    }

    /**
     * Sets the event identifier subject to the database uniqueness constraint.
     *
     * @param eventId the identifier shared by deliveries of the same event
     * @return this entity for chained updates
     */
    public PersonEntity setEventId(UUID eventId) {
        this.eventId = eventId;
        return this;
    }
}
