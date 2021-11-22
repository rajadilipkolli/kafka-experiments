package com.sivalabs.springbootkafka.multi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageRestController {

    @Autowired
    private Sender sender;

    @PostMapping("/messages/simple")
    public void sendSimpleMessage(@RequestBody SimpleMessage message) {
        sender.send(message.getKey(), message.getValue());
    }

    @PostMapping("/messages/json")
    public void sendJsonMessage(@RequestBody SimpleMessage message) {
        sender.send(message);
    }

}
