package com.example.outboxpattern.mapper;

import com.example.outboxpattern.entities.Order;
import com.example.outboxpattern.model.request.OrderRequest;
import com.example.outboxpattern.model.response.OrderResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderMapper {

    public Order toEntity(OrderRequest orderRequest) {
        Order order = new Order();
        order.setText(orderRequest.text());
        return order;
    }

    public void mapOrderWithRequest(Order order, OrderRequest orderRequest) {
        order.setText(orderRequest.text());
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(order.getId(), order.getText());
    }

    public List<OrderResponse> toResponseList(List<Order> orderList) {
        return orderList.stream().map(this::toResponse).toList();
    }
}
