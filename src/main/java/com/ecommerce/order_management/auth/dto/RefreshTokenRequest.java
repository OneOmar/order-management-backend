package com.ecommerce.order_management.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour refresh token
 */
public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token obligatoire")
        String refreshToken

) {}
