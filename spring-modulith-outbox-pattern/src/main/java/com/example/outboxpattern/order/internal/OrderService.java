package com.example.outboxpattern.order.internal;

import com.example.outboxpattern.config.Loggable;
import com.example.outboxpattern.order.OrderRecord;
import com.example.outboxpattern.order.internal.domain.query.FindOrdersQuery;
import com.example.outboxpattern.order.internal.domain.request.OrderRequest;
import com.example.outboxpattern.order.internal.domain.response.PagedResult;
import com.example.outboxpattern.order.internal.entities.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional(readOnly = true)
@Loggable
class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher events;

    /** Creates the order service with persistence, mapping, and event-publication dependencies. */
    OrderService(OrderRepository orderRepository, OrderMapper orderMapper, ApplicationEventPublisher events) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.events = events;
    }

    /** Returns a page of mapped orders using the requested pagination and sorting. */
    PagedResult<OrderRecord> findAllOrders(FindOrdersQuery findOrdersQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findOrdersQuery);

        Page<Order> ordersPage = orderRepository.findAll(pageable);

        List<OrderRecord> orderRecordList = orderMapper.toResponseList(ordersPage.getContent());

        return new PagedResult<>(ordersPage, orderRecordList);
    }

    /** Converts the requested page number to a nonnegative zero-based index and applies sorting. */
    private Pageable createPageable(FindOrdersQuery findOrdersQuery) {
        int pageNo = Math.max(findOrdersQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findOrdersQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findOrdersQuery.sortBy())
                        : Sort.Order.desc(findOrdersQuery.sortBy()));
        return PageRequest.of(pageNo, findOrdersQuery.pageSize(), sort);
    }

    /** Returns the mapped order when the identifier exists, or an empty result otherwise. */
    Optional<OrderRecord> findOrderById(Long id) {
        return orderRepository.findOrderById(id).map(orderMapper::toResponse);
    }

    /**
     * Persists a new order and publishes its mapped record within the same transaction.
     *
     * @param orderRequest the order details to persist
     * @return the saved order record
     */
    @Transactional
    OrderRecord saveOrder(OrderRequest orderRequest) {
        Order order = orderMapper.toEntity(orderRequest);
        Order savedOrder = orderRepository.save(order);
        Assert.notNull(savedOrder, () -> "SavedOrder can't be Null");
        OrderRecord orderRecord = orderMapper.toResponse(savedOrder);
        events.publishEvent(orderRecord);
        return orderRecord;
    }

    /**
     * Applies the requested changes to an existing order within a transaction.
     *
     * @param id the order identifier
     * @param orderRequest the replacement order details
     * @return the updated order record
     * @throws OrderNotFoundException if no order exists for the identifier
     */
    @Transactional
    OrderRecord updateOrder(Long id, OrderRequest orderRequest) {
        Order order = orderRepository.findOrderById(id).orElseThrow(() -> new OrderNotFoundException(id));

        // Update the order object with data from orderRequest
        orderMapper.mapOrderWithRequest(order, orderRequest);

        // Save the updated order object
        Order updatedOrder = orderRepository.save(order);

        Assert.notNull(updatedOrder, () -> "UpdatedOrder can't be Null");
        return orderMapper.toResponse(updatedOrder);
    }

    @Transactional
    void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }
}
