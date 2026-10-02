--liquibase formatted sql

--changeset kafka-avro:1
CREATE SEQUENCE person_entity_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE person_entity (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    age INTEGER,
    gender VARCHAR(255),
    email VARCHAR(255),
    phone_number VARCHAR(255),
    event_id UUID NOT NULL,
    CONSTRAINT uk_person_entity_event_id UNIQUE (event_id)
);
