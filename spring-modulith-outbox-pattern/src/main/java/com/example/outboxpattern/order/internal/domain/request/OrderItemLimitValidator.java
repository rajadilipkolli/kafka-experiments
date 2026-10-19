package com.example.outboxpattern.order.internal.domain.request;

import com.example.outboxpattern.config.ApplicationProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class OrderItemLimitValidator implements ConstraintValidator<OrderItemLimit, List<OrderItemRequest>> {

    private final int maxItemCount;

    public OrderItemLimitValidator(ApplicationProperties properties) {
        this.maxItemCount = properties.maxItemCount();
    }

    /**
     * Accepts null lists or lists at or below the configured item limit.
     * Larger lists receive a constraint violation containing the configured limit.
     *
     * @return whether the list satisfies the size limit; requiredness is validated separately
     */
    @Override
    public boolean isValid(List<OrderItemRequest> items, ConstraintValidatorContext context) {
        if (items == null || items.size() <= maxItemCount) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("ItemsList must not exceed %d items".formatted(maxItemCount))
                .addConstraintViolation();
        return false;
    }
}
