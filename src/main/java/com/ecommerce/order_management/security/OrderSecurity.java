package com.ecommerce.order_management.security;

import com.ecommerce.order_management.entity.Order;
import com.ecommerce.order_management.entity.User;
import com.ecommerce.order_management.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderService orderService;

    /**
     * Vérifie si l'utilisateur connecté est :
     * - propriétaire de la commande
     * - OU admin
     */
    public boolean isOwnerOrAdmin(Long orderId, Authentication authentication) {

        // récupérer user DIRECTEMENT depuis Spring Security
        User user = (User) authentication.getPrincipal();

        // récupérer la commande
        Order order = orderService.findById(orderId);

        // autorisé si owner ou admin
        return order.getUser().getId().equals(user.getId())
                || user.getRole().name().equals("ROLE_ADMIN");
    }
}