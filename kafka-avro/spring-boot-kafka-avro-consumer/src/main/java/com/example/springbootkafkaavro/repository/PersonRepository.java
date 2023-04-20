package com.example.springbootkafkaavro.repository;

import com.example.springbootkafkaavro.entity.PersonEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<PersonEntity, Long> {}
