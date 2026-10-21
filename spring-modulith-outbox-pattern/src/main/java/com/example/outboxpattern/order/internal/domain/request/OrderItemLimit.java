package com.example.outboxpattern.order.internal.domain.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderItemLimitValidator.class)
public @interface OrderItemLimit {
    String message() default "ItemsList exceeds the configured limit";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
