package com.sivalabs.springbootkafkasample;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageRestController {

    @Autowired
    private Sender sender;

    @PostMapping("/messages")
    public void sendMessage(@RequestBody Message message) {
        sender.send(message.getTopic(), message.getMsg());
    }

}

@Data
class Message {
    private String topic;
    private String msg;
}