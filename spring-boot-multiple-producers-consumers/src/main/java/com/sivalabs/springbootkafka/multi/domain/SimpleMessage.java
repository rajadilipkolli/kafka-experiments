package com.sivalabs.springbootkafka.multi.domain;

public record SimpleMessage(
        Integer key,
        String value) {
}
