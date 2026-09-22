package com.ecommerce.order_management.entity;

/** Statut d'une commande. */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}