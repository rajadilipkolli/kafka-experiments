package com.example.outboxpattern.order.internal;

import com.example.outboxpattern.config.Loggable;
import com.example.outboxpattern.order.OrderRecord;
import com.example.outboxpattern.order.internal.domain.query.FindOrdersQuery;
import com.example.outboxpattern.order.internal.domain.request.OrderRequest;
import com.example.outboxpattern.order.internal.domain.response.PagedResult;
import com.example.outboxpattern.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/orders")
@Loggable
class OrderController {

    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    PagedResult<OrderRecord> getAllOrders(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        FindOrdersQuery findOrdersQuery = new FindOrdersQuery(pageNo, pageSize, sortBy, sortDir);
        return orderService.findAllOrders(findOrdersQuery);
    }

    @GetMapping("/{id}")
    ResponseEntity<OrderRecord> getOrderById(@PathVariable Long id) {
        return orderService.findOrderById(id).map(ResponseEntity::ok).orElseThrow(() -> new OrderNotFoundException(id));
    }

    /**
     * Creates an order from a validated JSON request.
     *
     * @param orderRequest order details to persist
     * @return a 201 Created response containing the saved order and its URI in the Location header
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OrderRecord> createOrder(@RequestBody @Validated OrderRequest orderRequest) {
        OrderRecord response = orderService.saveOrder(orderRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Updates an existing order from a validated JSON request.
     *
     * @param id identifier of the order to update
     * @param orderRequest replacement order details
     * @return a 200 OK response containing the updated order
     * @throws OrderNotFoundException if no order exists for the identifier
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OrderRecord> updateOrder(@PathVariable Long id, @RequestBody @Valid OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.updateOrder(id, orderRequest));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<OrderRecord> deleteOrder(@PathVariable Long id) {
        return orderService
                .findOrderById(id)
                .map(order -> {
                    orderService.deleteOrderById(id);
                    return ResponseEntity.ok(order);
                })
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
