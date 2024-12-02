package com.example.springbootkafkasample.dto;

public record KafkaListenerRequest(String containerId, Operation operation) {}
