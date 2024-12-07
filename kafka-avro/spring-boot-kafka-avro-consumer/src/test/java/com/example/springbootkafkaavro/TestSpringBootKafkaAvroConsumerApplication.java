package com.example.springbootkafkaavro;

import com.example.springbootkafkaavro.common.KafkaContainersConfig;
import org.springframework.boot.SpringApplication;

class TestSpringBootKafkaAvroConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringBootKafkaAvroConsumerApplication::main)
                .with(KafkaContainersConfig.class)
                .run(args);
    }
}
