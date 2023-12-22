package com.example.outboxpattern.order.internal.query;

public record FindOrdersQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
