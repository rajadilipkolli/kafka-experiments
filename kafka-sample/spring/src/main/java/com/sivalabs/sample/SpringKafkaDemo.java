package com.sivalabs.sample;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public class SpringKafkaDemo {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(KafkaConfig.class);

        MessageSender messageSender = ctx.getBean(MessageSender.class);
        SecureRandom random = new SecureRandom();
        while(true) {
            messageSender.send(String.valueOf(random.nextInt()),String.valueOf(random.nextInt()));
            TimeUnit.SECONDS.sleep(10);
        }
    }
}
