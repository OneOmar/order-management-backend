package com.ecommerce.order_management.auth.dto;

/**
 * DTO de Réponse d'authentification :
 * retourne tokens + infos utilisateur
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String role
) {}