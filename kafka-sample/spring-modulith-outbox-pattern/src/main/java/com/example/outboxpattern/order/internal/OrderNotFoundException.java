package com.example.outboxpattern.order.internal;

import com.example.outboxpattern.exception.ResourceNotFoundException;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(Long id) {
        super("Order with Id '%d' Not found".formatted(id));
    }
}
