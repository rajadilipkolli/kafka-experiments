package com.example.outboxpattern.order.internal.domain.query;

public record FindOrdersQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
