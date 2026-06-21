package com.example.outboxpattern.common;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TestDataHelper {

    private final JdbcTemplate jdbcTemplate;

    public TestDataHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void deleteAllOrderItems() {
        jdbcTemplate.update("DELETE FROM order_items");
    }

    public void deleteAllOrders() {
        jdbcTemplate.update("DELETE FROM orders");
    }

    public Long insertOrder(LocalDateTime orderedDate, String status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO orders (ordered_date, status) VALUES (?, ?)", new String[] {"id"});
                    ps.setTimestamp(1, Timestamp.valueOf(orderedDate));
                    ps.setString(2, status);
                    return ps;
                },
                keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Long insertOrderItem(Long orderId, String productCode, BigDecimal price, int quantity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO order_items (order_id, product_code, product_price, quantity) VALUES (?, ?, ?, ?)",
                            new String[] {"id"});
                    ps.setLong(1, orderId);
                    ps.setString(2, productCode);
                    ps.setBigDecimal(3, price);
                    ps.setInt(4, quantity);
                    return ps;
                },
                keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Map<String, Object> findOrderById(Long id) {
        return jdbcTemplate.queryForMap("SELECT * FROM orders WHERE id = ?", id);
    }
}
