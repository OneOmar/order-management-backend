package com.ecommerce.order_management.controller;

import com.ecommerce.order_management.dto.request.OrderRequestDTO;
import com.ecommerce.order_management.dto.response.OrderResponseDTO;
import com.ecommerce.order_management.entity.Order;
import com.ecommerce.order_management.entity.OrderItem;
import com.ecommerce.order_management.entity.Product;
import com.ecommerce.order_management.entity.User;
import com.ecommerce.order_management.mapper.OrderMapper;
import com.ecommerce.order_management.service.OrderService;
import com.ecommerce.order_management.service.UserService;
import jakarta.validation.Valid;
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
    public ResponseEntity<OrderResponseDTO> create(
            Authentication authentication,
            @Valid @RequestBody OrderRequestDTO request
    ) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        Order order = mapToOrder(request);
        Order saved = orderService.createOrder(user.getId(), order);

        return ResponseEntity.status(201).body(OrderMapper.toDTO(saved));
    }

    /**
     * USER connecté
     * Récupérer ses commandes
     */
    @GetMapping("/me")
    public ResponseEntity<List<OrderResponseDTO>> myOrders(Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        List<OrderResponseDTO> orders = orderService.findByUserId(user.getId())
                .stream()
                .map(OrderMapper::toDTO)
                .toList();

        return ResponseEntity.ok(orders);
    }

    /**
     * USER ou ADMIN
     * Récupérer une commande par ID (sécurisé)
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Order order = orderService.findById(id);

        String email = authentication.getName();
        User currentUser = userService.findByEmail(email);

        boolean isOwner = order.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");

        // FIXED condition
        if (!(isOwner || isAdmin)) {
            throw new AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(OrderMapper.toDTO(order));
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

        Order order = orderService.findById(id);

        String email = authentication.getName();
        User currentUser = userService.findByEmail(email);

        boolean isOwner = order.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");

        if (!(isOwner || isAdmin)) {
            throw new AccessDeniedException("Access denied");
        }

        orderService.cancelOrder(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Convertir un OrderRequestDTO en entité Order.
     */
    private Order mapToOrder(OrderRequestDTO req) {

        // Transformer les items du DTO en OrderItem (entity)
        List<OrderItem> items = req.items().stream()
                .map(item -> OrderItem.builder()
                        .product(Product.builder()
                                .id(item.productId())
                                .build())
                        .quantity(item.quantity())
                        .build())
                .toList();

        // Construire l'entité Order
        return Order.builder()
                .shippingAddress(req.shippingAddress())
                .items(items)
                .build();
    }
}