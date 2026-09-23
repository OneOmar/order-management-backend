package com.ecommerce.order_management.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour login utilisateur
 */
public record LoginRequest(

        @Email(message = "Email invalide")
        @NotBlank(message = "Email obligatoire")
        String email,

        @NotBlank(message = "Mot de passe obligatoire")
        String password

) {}