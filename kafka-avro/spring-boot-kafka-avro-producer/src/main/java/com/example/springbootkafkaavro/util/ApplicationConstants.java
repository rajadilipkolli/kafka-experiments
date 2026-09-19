package com.example.springbootkafkaavro.util;

public class ApplicationConstants {

    public static final String PERSONS_TOPIC = "persons";

    private ApplicationConstants() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }
}
