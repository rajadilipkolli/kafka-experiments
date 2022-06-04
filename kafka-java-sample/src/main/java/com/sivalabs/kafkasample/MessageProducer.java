package com.sivalabs.kafkasample;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;

public class MessageProducer {

    private final String TOPIC_NAME;

    MessageProducer(String topic_name) {
        TOPIC_NAME = topic_name;
    }

    public static void main(String[] args) {
        MessageProducer producer = new MessageProducer("test");
        producer.run();
    }

    void run() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            //Long count = 0L;
            String[] names = {"Siva","Neha","Ramu","Suman"};
            Random random = new Random();
            while (true) {

                String name = names[random.nextInt(names.length)];
                producer.send(new ProducerRecord<>(TOPIC_NAME, name, name));
                System.out.println("Sent Name="+ name);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //count++;
            }
        }

        //System.out.println("Message sent successfully");
        //producer.close();

    }
}
