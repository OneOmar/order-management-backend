package com.ecommerce.order_management.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO pour un item de commande
 */
public record CreateOrderItemRequest(

        @NotNull(message = "Product ID requis")
        Long productId,

        @Min(value = 1, message = "Quantité >= 1")
        Integer quantity

) {}