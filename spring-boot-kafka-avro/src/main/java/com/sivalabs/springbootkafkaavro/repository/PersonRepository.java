package com.sivalabs.springbootkafkaavro.repository;

import com.sivalabs.springbootkafkaavro.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Integer> {}
