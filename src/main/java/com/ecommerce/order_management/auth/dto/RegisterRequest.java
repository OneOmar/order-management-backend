package com.ecommerce.order_management.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour inscription utilisateur
 */
public record RegisterRequest(

        @NotBlank(message = "Prenom obligatoire")
        String firstName,

        @NotBlank(message = "Nom obligatoire")
        String lastName,

        @Email(message = "Email invalide")
        @NotBlank(message = "Email obligatoire")
        String email,

        @NotBlank(message = "Mot de passe obligatoire")
        @Size(min = 6, message = "Mot de passe min 6 caracteres")
        String password

) {}
