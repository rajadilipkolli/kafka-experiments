package com.example.outboxpattern.order.internal;

import com.example.outboxpattern.config.Loggable;
import com.example.outboxpattern.order.OrderItemRecord;
import com.example.outboxpattern.order.OrderRecord;
import com.example.outboxpattern.order.internal.domain.request.OrderItemRequest;
import com.example.outboxpattern.order.internal.domain.request.OrderRequest;
import com.example.outboxpattern.order.internal.entities.Order;
import com.example.outboxpattern.order.internal.entities.OrderItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Loggable
class OrderMapper {

    Order toEntity(OrderRequest orderRequest) {
        Order order = new Order().setOrderedDate(LocalDateTime.now());
        if (StringUtils.hasText(orderRequest.status())) {
            order.setStatus(Order.OrderStatus.valueOf(orderRequest.status()));
        } else {
            order.setStatus(Order.OrderStatus.CREATED);
        }
        convertToOrderItemEntityList(orderRequest.itemsList()).forEach(order::addOrderItem);
        return order;
    }

    private List<OrderItem> convertToOrderItemEntityList(List<OrderItemRequest> orderItemRequests) {
        return orderItemRequests.stream().map(this::convertToOrderItemEntity).toList();
    }

    private OrderItem convertToOrderItemEntity(OrderItemRequest orderItemRequest) {
        return new OrderItem()
                .setProductCode(orderItemRequest.productCode())
                .setProductPrice(orderItemRequest.productPrice())
                .setQuantity(orderItemRequest.quantity());
    }

    void mapOrderWithRequest(Order order, OrderRequest orderRequest) {
        if (StringUtils.hasText(orderRequest.status())) {
            order.setStatus(Order.OrderStatus.valueOf(orderRequest.status()));
        } else {
            order.setStatus(Order.OrderStatus.COMPLETED);
        }
        // Convert request to OrderItems
        List<OrderItem> detachedOrderItems = convertToOrderItemEntityList(orderRequest.itemsList());

        // Remove the existing database rows that are no
        // longer found in the incoming collection (detachedOrderItems)
        List<OrderItem> orderItemsToRemove = order.getItems().stream()
                .filter(orderItem -> !detachedOrderItems.contains(orderItem))
                .toList();
        orderItemsToRemove.forEach(order::removeOrderItem);

        // Update the existing database rows which can be found
        // in the incoming collection (detachedOrderItems)
        List<OrderItem> newOrderItems = detachedOrderItems.stream()
                .filter(orderItem -> !order.getItems().contains(orderItem))
                .toList();

        detachedOrderItems.stream()
                .filter(orderItem -> !newOrderItems.contains(orderItem))
                .forEach(orderItem -> {
                    orderItem.setOrder(order);
                    orderItem.setId(getOrderItemId(order.getItems(), orderItem));
                    order.getItems().set(order.getItems().indexOf(orderItem), orderItem);
                });

        // Add the rows found in the incoming collection,
        // which cannot be found in the current database snapshot
        newOrderItems.forEach(order::addOrderItem);
    }

    // Manual Merge instead of using `var mergedBook = orderItemRepository.save(orderItem)` which calls save in middle
    // of transaction
    private Long getOrderItemId(List<OrderItem> items, OrderItem orderItem) {
        return items.stream()
                .filter(item -> Objects.equals(item.getProductCode(), orderItem.getProductCode()))
                .map(OrderItem::getId)
                .findFirst()
                .orElse(null);
    }

    OrderRecord toResponse(Order order) {
        return new OrderRecord(
                order.getId(),
                order.getOrderedDate(),
                order.getStatus().name(),
                convertToOrderItemRecordList(order.getItems()));
    }

    private List<OrderItemRecord> convertToOrderItemRecordList(List<OrderItem> items) {
        return items.stream().map(this::convertToOrderItemRecord).toList();
    }

    private OrderItemRecord convertToOrderItemRecord(OrderItem orderItem) {
        return new OrderItemRecord(orderItem.getProductCode(), orderItem.getProductPrice(), orderItem.getQuantity());
    }

    List<OrderRecord> toResponseList(List<Order> orderList) {
        return orderList.stream().map(this::toResponse).toList();
    }
}
