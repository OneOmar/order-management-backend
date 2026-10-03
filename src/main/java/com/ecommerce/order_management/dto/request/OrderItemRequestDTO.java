package com.ecommerce.order_management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDTO(

        @NotNull(message = "productId requis")
        Long productId,

        @NotNull(message = "quantité requise")
        @Min(value = 1, message = "quantité doit être >= 1")
        Integer quantity

) {
}