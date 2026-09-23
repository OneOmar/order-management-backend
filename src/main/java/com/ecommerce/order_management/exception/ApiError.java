package com.ecommerce.order_management.exception;

import java.time.Instant;

/**
 * DTO pour répondre aux erreurs de l'API de façon typée et claire.
 * Utilise un record Java (Java 17+) : simple, immuable et facile à sérialiser.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}