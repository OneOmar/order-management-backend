package com.ecommerce.order_management.exception;

/**
 * Exception levée quand une ressource n'est pas trouvée (404).
 * RuntimeException pour être facilement propagée et convertie en réponse HTTP
 * via un @ControllerAdvice.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}