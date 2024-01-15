package com.example.outboxpattern.order;

import java.math.BigDecimal;

public record OrderItemRecord(String productCode, BigDecimal productPrice, int quantity) {}
