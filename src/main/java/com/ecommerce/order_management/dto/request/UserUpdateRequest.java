package com.ecommerce.order_management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO pour UPDATE user (input API)
 */
public record UserUpdateRequest(

        @Size(min = 2, message = "First name too short")
        String firstName,

        @Size(min = 2, message = "Last name too short")
        String lastName,

        @Email(message = "Invalid email")
        String email,

        @Size(min = 8, message = "Password must be >= 8 chars")
        String password,

        String role

) {}