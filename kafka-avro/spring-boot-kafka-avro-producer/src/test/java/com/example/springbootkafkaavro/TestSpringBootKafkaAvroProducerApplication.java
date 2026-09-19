package com.example.springbootkafkaavro;

import com.example.springbootkafkaavro.containers.KafkaContainersConfig;
import org.springframework.boot.SpringApplication;

class TestSpringBootKafkaAvroProducerApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringBootKafkaAvroProducerApplication::main)
                .with(KafkaContainersConfig.class)
                .run(args);
    }
}
