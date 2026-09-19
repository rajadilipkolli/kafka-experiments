package com.example.outboxpattern.order.internal;

import com.example.outboxpattern.exception.ResourceNotFoundException;

class OrderNotFoundException extends ResourceNotFoundException {

    OrderNotFoundException(Long id) {
        super("Order with Id '%d' Not found".formatted(id));
    }
}
