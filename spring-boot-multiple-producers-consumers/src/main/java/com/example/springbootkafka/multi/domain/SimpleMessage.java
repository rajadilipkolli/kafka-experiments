package com.example.springbootkafka.multi.domain;

public record SimpleMessage(
        Integer key,
        String value) {
}
