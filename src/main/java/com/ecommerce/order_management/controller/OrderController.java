package com.ecommerce.order_management.controller;

import com.ecommerce.order_management.dto.request.CreateOrderRequest;
import com.ecommerce.order_management.dto.response.OrderItemDTO;
import com.ecommerce.order_management.dto.response.OrderResponseDTO;
import com.ecommerce.order_management.entity.Order;
import com.ecommerce.order_management.entity.OrderItem;
import com.ecommerce.order_management.entity.Product;
import com.ecommerce.order_management.entity.User;
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
            @Valid @RequestBody CreateOrderRequest request
    ) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        Order order = mapToOrder(request);
        Order saved = orderService.createOrder(user.getId(), order);

        return ResponseEntity.ok(map(saved));
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
                .map(this::map)
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

        return ResponseEntity.ok(map(order));
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

    // ================= DTO MAPPERS =================

    private OrderResponseDTO map(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getEmail(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getShippingAddress(),
                order.getCreatedAt(),
                order.getItems().stream().map(this::mapItem).toList()
        );
    }

    private OrderItemDTO mapItem(OrderItem item) {
        return new OrderItemDTO(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice()
        );
    }

    private Order mapToOrder(CreateOrderRequest req) {
        List<OrderItem> items = req.items().stream()
                .map(i -> OrderItem.builder()
                        .product(Product.builder().id(i.productId()).build())
                        .quantity(i.quantity())
                        .build()
                )
                .toList();

        return Order.builder()
                .shippingAddress(req.shippingAddress())
                .items(items)
                .build();
    }
}