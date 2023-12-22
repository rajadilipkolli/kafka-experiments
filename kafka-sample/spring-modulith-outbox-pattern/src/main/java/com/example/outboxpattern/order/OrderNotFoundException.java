package com.example.outboxpattern.order;

import com.example.outboxpattern.exception.ResourceNotFoundException;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(Long id) {
        super("Order with Id '%d' not found".formatted(id));
    }
}
