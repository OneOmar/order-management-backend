package com.ecommerce.order_management.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO pour créer une commande (safe)
 */
public record CreateOrderRequest(

        @NotBlank(message = "Adresse requise")
        String shippingAddress,

        @NotEmpty(message = "Items requis")
        List<CreateOrderItemRequest> items

) {}