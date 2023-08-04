/* (C)2023 */
package com.example.cloudkafkasample.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cloudkafkasample.domain.MessageDTO;
import java.util.Locale;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class SenderTest {

    @EnableAutoConfiguration
    public static class MyTestConfiguration {

        @Bean
        public Function<MessageDTO, String> receiveAndConvert() {
            return data -> data.msg().toUpperCase(Locale.ROOT);
        }
    }

    @Test
    public void sampleTest() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                        TestChannelBinderConfiguration.getCompleteConfiguration(MyTestConfiguration.class))
                .run("--spring.cloud.function.definition=receiveAndConvert")) {
            InputDestination source = context.getBean(InputDestination.class);
            OutputDestination target = context.getBean(OutputDestination.class);
            source.send(new GenericMessage<>(new MessageDTO("junit")), "receiveAndConvert-in-0");
            assertThat(target.receive(100, "receiveAndConvert-out-0").getPayload())
                    .isEqualTo("JUNIT".getBytes());
        }
    }
}
