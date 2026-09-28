package com.ecommerce.order_management.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour exposer une commande
 */
public record OrderResponseDTO(

        Long id,
        String userEmail,
        BigDecimal totalAmount,
        String status,
        String shippingAddress,
        LocalDateTime createdAt,

        List<OrderItemDTO> items

) {}