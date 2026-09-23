package com.ecommerce.order_management.exception;

/**
 * Exception levée quand le stock est insuffisant pour satisfaire une commande.
 * RuntimeException pour faciliter la propagation et la transformation en réponse HTTP.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}