package com.ecommerce.order_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequestDTO(

        @NotBlank(message = "Adresse de livraison requise")
        String shippingAddress,

        @NotEmpty(message = "La commande doit contenir au moins un item")
        List<OrderItemRequestDTO> items

) {
}