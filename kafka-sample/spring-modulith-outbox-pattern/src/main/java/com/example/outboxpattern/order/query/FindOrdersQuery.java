package com.example.outboxpattern.order.query;

public record FindOrdersQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
