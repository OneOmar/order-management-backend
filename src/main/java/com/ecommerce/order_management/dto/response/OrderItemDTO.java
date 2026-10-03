package com.ecommerce.order_management.dto.response;

import java.math.BigDecimal;

public record OrderItemDTO(

        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice

) {
}