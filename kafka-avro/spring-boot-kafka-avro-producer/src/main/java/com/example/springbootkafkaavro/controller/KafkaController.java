package com.example.springbootkafkaavro.controller;

import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.service.KafkaProducer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/person")
class KafkaController {

    private final KafkaProducer producer;

    KafkaController(KafkaProducer producer) {
        this.producer = producer;
    }

    @PostMapping(value = "/publish")
    void sendMessageToKafkaTopic(
            @RequestParam String name,
            @RequestParam Integer age,
            @RequestParam(required = false) String gender) {
        Person person = new Person();
        person.setAge(age);
        person.setName(name);
        if (gender != null) {
            person.setGender(gender);
        }
        this.producer.sendMessage(person);
    }
}
