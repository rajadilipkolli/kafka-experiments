package com.example.quarkuskafkademo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageDto {

    private final String message;
    private final int id;

    @JsonCreator
    public MessageDto(@JsonProperty("message") String message, @JsonProperty("id") int id) {
        this.message = message;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "MessageDto{" + "message='" + message + '\'' + ", id=" + id + '}';
    }
}
