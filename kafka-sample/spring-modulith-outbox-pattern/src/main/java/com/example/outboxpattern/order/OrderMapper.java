package com.example.outboxpattern.order;

import com.example.outboxpattern.order.request.OrderRequest;
import com.example.outboxpattern.order.response.OrderResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
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
