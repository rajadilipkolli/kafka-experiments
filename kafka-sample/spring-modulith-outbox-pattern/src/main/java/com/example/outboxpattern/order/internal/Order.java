package com.example.outboxpattern.order.internal;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String product;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Order(String product) {
        this.product = product;
        this.status = OrderStatus.CREATED;
    }

    public Order setProduct(String product) {
        this.product = product;
        return this;
    }

    public enum OrderStatus {
        CREATED,
        COMPLETED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Order order = (Order) o;
        return id != null && Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
