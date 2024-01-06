package com.example.outboxpattern.order.internal;

import com.example.outboxpattern.config.Loggable;
import com.example.outboxpattern.order.OrderResponse;
import com.example.outboxpattern.order.internal.query.FindOrdersQuery;
import com.example.outboxpattern.order.internal.request.OrderRequest;
import com.example.outboxpattern.order.internal.response.PagedResult;
import com.example.outboxpattern.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Loggable
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public PagedResult<OrderResponse> getAllOrders(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        FindOrdersQuery findOrdersQuery = new FindOrdersQuery(pageNo, pageSize, sortBy, sortDir);
        return orderService.findAllOrders(findOrdersQuery);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return orderService.findOrderById(id).map(ResponseEntity::ok).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Validated OrderRequest orderRequest) {
        OrderResponse response = orderService.saveOrder(orderRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/orders/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id, @RequestBody @Valid OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.updateOrder(id, orderRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> deleteOrder(@PathVariable Long id) {
        return orderService
                .findOrderById(id)
                .map(order -> {
                    orderService.deleteOrderById(id);
                    return ResponseEntity.ok(order);
                })
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
