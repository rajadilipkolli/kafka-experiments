package com.sivalabs.springbootkafka.multi;

import lombok.Data;

@Data
public class SimpleMessage {
    private Integer key;
    private String value;
}
