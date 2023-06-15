package com.example.boot.kafka.reactor;

import com.example.boot.kafka.reactor.config.MyTestContainers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@Import(MyTestContainers.class)
public class TestBootKafkaReactorProducerApplication {

    public static void main(String[] args) {
        SpringApplication.from(BootKafkaReactorProducerApplication::main)
                .with(TestBootKafkaReactorProducerApplication.class)
                .run(args);
    }
}
