package com.ecommerce.order_management.dto.response;

// DTO pour exposer uniquement les infos safe
public record UserResponseDTO(

        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean enabled

) {}