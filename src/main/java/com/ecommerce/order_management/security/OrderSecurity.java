package com.ecommerce.order_management.security;

import com.ecommerce.order_management.entity.Order;
import com.ecommerce.order_management.entity.User;
import com.ecommerce.order_management.service.OrderService;
import com.ecommerce.order_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderService orderService;
    private final UserService userService;

    /**
     * Vérifie si l'utilisateur connecté est :
     * - propriétaire de la commande
     * - OU admin
     */
    public boolean isOwnerOrAdmin(Long orderId, Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        Order order = orderService.findById(orderId);

        // true si owner OU admin
        return order.getUser().getId().equals(user.getId())
                || user.getRole().name().equals("ROLE_ADMIN");
    }
}