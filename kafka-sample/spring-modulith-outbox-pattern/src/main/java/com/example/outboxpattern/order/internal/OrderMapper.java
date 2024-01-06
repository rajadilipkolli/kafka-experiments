package com.example.outboxpattern.order.internal;

import com.example.outboxpattern.config.Loggable;
import com.example.outboxpattern.order.OrderResponse;
import com.example.outboxpattern.order.internal.request.OrderRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Loggable
class OrderMapper {

    Order toEntity(OrderRequest orderRequest) {
        return new Order(orderRequest.product());
    }

    void mapOrderWithRequest(Order order, OrderRequest orderRequest) {
        order.setProduct(orderRequest.product());
    }

    OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(), order.getProduct(), order.getStatus().name());
    }

    List<OrderResponse> toResponseList(List<Order> orderList) {
        return orderList.stream().map(this::toResponse).toList();
    }
}
