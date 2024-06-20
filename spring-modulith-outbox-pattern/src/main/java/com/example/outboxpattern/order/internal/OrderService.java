package com.example.outboxpattern.order.internal;

import com.example.outboxpattern.config.Loggable;
import com.example.outboxpattern.order.OrderRecord;
import com.example.outboxpattern.order.internal.domain.query.FindOrdersQuery;
import com.example.outboxpattern.order.internal.domain.request.OrderRequest;
import com.example.outboxpattern.order.internal.domain.response.PagedResult;
import com.example.outboxpattern.order.internal.entities.Order;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Loggable
class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher events;
    private final TransactionTemplate transactionTemplate;

    @PostConstruct
    void setPropagation() {
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
    }

    PagedResult<OrderRecord> findAllOrders(FindOrdersQuery findOrdersQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findOrdersQuery);

        Page<Order> ordersPage = orderRepository.findAll(pageable);

        List<OrderRecord> orderRecordList = orderMapper.toResponseList(ordersPage.getContent());

        return new PagedResult<>(ordersPage, orderRecordList);
    }

    private Pageable createPageable(FindOrdersQuery findOrdersQuery) {
        int pageNo = Math.max(findOrdersQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findOrdersQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findOrdersQuery.sortBy())
                        : Sort.Order.desc(findOrdersQuery.sortBy()));
        return PageRequest.of(pageNo, findOrdersQuery.pageSize(), sort);
    }

    Optional<OrderRecord> findOrderById(Long id) {
        return orderRepository.findOrderById(id).map(orderMapper::toResponse);
    }

    OrderRecord saveOrder(OrderRequest orderRequest) {
        Order order = orderMapper.toEntity(orderRequest);
        Order savedOrder = getSavedOrder(order);
        Assert.notNull(savedOrder, () -> "SavedOrder can't be Null");
        OrderRecord orderRecord = orderMapper.toResponse(savedOrder);
        events.publishEvent(orderRecord);
        return orderRecord;
    }

    private Order getSavedOrder(Order order) {
        return transactionTemplate.execute(status -> orderRepository.save(order));
    }

    OrderRecord updateOrder(Long id, OrderRequest orderRequest) {
        Order order = orderRepository.findOrderById(id).orElseThrow(() -> new OrderNotFoundException(id));

        // Update the order object with data from orderRequest
        orderMapper.mapOrderWithRequest(order, orderRequest);

        // Save the updated order object
        Order updatedOrder = getSavedOrder(order);

        Assert.notNull(updatedOrder, () -> "UpdatedOrder can't be Null");
        return orderMapper.toResponse(updatedOrder);
    }

    @Transactional
    void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }
}
