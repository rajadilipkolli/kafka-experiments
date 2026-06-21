package com.example.outboxpattern.common;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TestDataHelper {

    private final JdbcTemplate jdbcTemplate;

    public TestDataHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final AtomicLong orderIdGenerator = new AtomicLong(100_000L);
    private final AtomicLong orderItemIdGenerator = new AtomicLong(100_000L);

    public void deleteAllOrderItems() {
        jdbcTemplate.update("DELETE FROM order_items");
    }

    public void deleteAllOrders() {
        jdbcTemplate.update("DELETE FROM orders");
    }

    public Long insertOrder(LocalDateTime orderedDate, String status) {
        Long id = orderIdGenerator.incrementAndGet();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps =
                    connection.prepareStatement("INSERT INTO orders (id, ordered_date, status) VALUES (?, ?, ?)");
            ps.setLong(1, id);
            ps.setTimestamp(2, Timestamp.valueOf(orderedDate));
            ps.setString(3, status);
            return ps;
        });
        return id;
    }

    public Long insertOrderItem(Long orderId, String productCode, BigDecimal price, int quantity) {
        Long id = orderItemIdGenerator.incrementAndGet();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO order_items (id, order_id, product_code, product_price, quantity) VALUES (?, ?, ?, ?, ?)");
            ps.setLong(1, id);
            ps.setLong(2, orderId);
            ps.setString(3, productCode);
            ps.setBigDecimal(4, price);
            ps.setInt(5, quantity);
            return ps;
        });
        return id;
    }

    public Map<String, Object> findOrderById(Long id) {
        return jdbcTemplate.queryForMap("SELECT * FROM orders WHERE id = ?", id);
    }
}
