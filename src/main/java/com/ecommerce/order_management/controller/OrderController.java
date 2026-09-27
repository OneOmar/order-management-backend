package com.ecommerce.order_management.controller;

import com.ecommerce.order_management.entity.Order;
import com.ecommerce.order_management.entity.User;
import com.ecommerce.order_management.service.OrderService;
import com.ecommerce.order_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    /**
     * USER connecté
     * Créer une commande
     */
    @PostMapping
    public ResponseEntity<Order> create(
            Authentication authentication,
            @RequestBody Order orderRequest
    ) {

        // 1. récupérer email depuis JWT
        String email = authentication.getName();

        // 2. récupérer user réel depuis DB
        User user = userService.findByEmail(email);

        // 3. utiliser son ID
        Order order = orderService.createOrder(user.getId(), orderRequest);

        return ResponseEntity.ok(order);
    }

    /**
     * USER connecté
     * Récupérer ses commandes
     */
    @GetMapping("/me")
    public ResponseEntity<List<Order>> myOrders(Authentication authentication) {

        // 1. email depuis JWT
        String email = authentication.getName();

        // 2. récupérer user
        User user = userService.findByEmail(email);

        // 3. récupérer ses commandes
        List<Order> orders = orderService.findByUserId(user.getId());

        return ResponseEntity.ok(orders);
    }

    /**
     * USER ou ADMIN
     * Récupérer une commande par ID (sécurisé)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        // 1. récupérer commande
        Order order = orderService.findById(id);

        // 2. récupérer user connecté
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email);

        // 3. check sécurité 🔒
        boolean isOwner = order.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");

        if (!isOwner || !isAdmin) {
            throw new AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(order);
    }

    /**
     * USER ou ADMIN
     * Annuler une commande
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            Authentication authentication
    ) {

        // 1. récupérer commande
        Order order = orderService.findById(id);

        // 2. user connecté
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email);

        // 3. sécurité
        boolean isOwner = order.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");

        if (!(isOwner || isAdmin)) {
            throw new AccessDeniedException("Access denied");
        }

        // 4. cancel
        orderService.cancelOrder(id);

        return ResponseEntity.noContent().build();
    }
}