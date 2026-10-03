package com.ecommerce.order_management.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequestDTO(

        @NotBlank(message = "Le nom est requis")
        String name,

        @Size(max = 1000, message = "Description trop longue")
        String description,

        @NotNull(message = "Prix requis")
        @DecimalMin(value = "0.0", inclusive = false, message = "Prix invalide")
        BigDecimal price,

        @NotNull(message = "Stock requis")
        @Min(value = 0, message = "Stock invalide")
        Integer stock,

        String imageUrl,

        @NotBlank(message = "Catégorie requise")
        String category,

        Boolean active
) {
}