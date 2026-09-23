package com.ecommerce.order_management.service;

import com.ecommerce.order_management.entity.Order;
import com.ecommerce.order_management.entity.OrderStatus;

import java.util.List;

/**
 * Contrat pour la couche service Commande.
 * - création de commande (vérification stock, calcul total)
 * - lecture par id / utilisateur / statut
 * - annulation / mise à jour de statut
 */
public interface OrderService {

    /**
     * Crée une commande pour l'utilisateur donné (userId).
     * Implémentation : valider stock, décrémenter, calculer total, sauvegarder.
     */
    Order createOrder(Long userId, Order orderRequest);

    Order findById(Long id);

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    /**
     * Met à jour le statut d'une commande (ex: CONFIRMED, SHIPPED, CANCELLED).
     */
    Order updateStatus(Long orderId, OrderStatus newStatus);

    /**
     * Annule une commande si possible (restock si nécessaire).
     */
    void cancelOrder(Long orderId);
}