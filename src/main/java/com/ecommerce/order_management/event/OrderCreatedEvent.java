package com.ecommerce.order_management.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        String email,
        Long orderId,
        BigDecimal totalAmount
) {
}