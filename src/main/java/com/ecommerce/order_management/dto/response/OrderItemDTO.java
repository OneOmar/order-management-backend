package com.ecommerce.order_management.dto.response;

import java.math.BigDecimal;

/**
 * DTO pour une ligne de commande
 */
public record OrderItemDTO(

        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice

) {}