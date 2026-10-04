package com.ecommerce.order_management.service;

import com.ecommerce.order_management.entity.Order;
import com.ecommerce.order_management.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contrat pour la couche service Commande.
 * - création de commande (vérification stock, calcul total)
 * - lecture par id / utilisateur / statut
 * - annulation / mise à jour de statut
 */
public interface OrderService {


    Order createOrder(Long userId, Order orderRequest);

    Order findById(Long id);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    Order updateStatus(Long orderId, OrderStatus newStatus);

    Page<Order> findByUserIdAndDateRange(
            Long userId,
            String startDate,
            String endDate,
            Pageable pageable
    );

    Page<Order> findByUserIdAndStatusAndDateRange(
            Long userId,
            String status,
            String startDate,
            String endDate,
            Pageable pageable
    );

    void cancelOrder(Long orderId);
}