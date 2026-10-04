package com.ecommerce.order_management.controller;

import com.ecommerce.order_management.dto.request.OrderRequestDTO;
import com.ecommerce.order_management.dto.response.OrderResponseDTO;
import com.ecommerce.order_management.entity.Order;
import com.ecommerce.order_management.entity.OrderItem;
import com.ecommerce.order_management.entity.Product;
import com.ecommerce.order_management.entity.User;
import com.ecommerce.order_management.mapper.OrderMapper;
import com.ecommerce.order_management.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Créer une commande pour l'utilisateur connecté
     * User injecté directement
     */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderRequestDTO request
    ) {
        Order order = mapToOrder(request);

        Order saved = orderService.createOrder(user.getId(), order);

        return ResponseEntity
                .status(201)
                .body(OrderMapper.toDTO(saved));
    }

    /**
     * Récupérer les commandes du user (pagination + filtres)
     */
    @GetMapping("/me")
    public ResponseEntity<Page<OrderResponseDTO>> myOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        // pagination + tri (récent -> ancien)
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<OrderResponseDTO> orders;

        // CAS 1 — status + date
        if (status != null && startDate != null && endDate != null) {
            orders = orderService
                    .findByUserIdAndStatusAndDateRange(
                            user.getId(),
                            status,
                            startDate,
                            endDate,
                            pageable
                    )
                    .map(OrderMapper::toDTO);
        }

        // CAS 2 — date only
        else if (startDate != null && endDate != null) {
            orders = orderService
                    .findByUserIdAndDateRange(
                            user.getId(),
                            startDate,
                            endDate,
                            pageable
                    )
                    .map(OrderMapper::toDTO);
        }

        // CAS 3 — status only
        else if (status != null) {
            orders = orderService
                    .findByUserIdAndStatus(
                            user.getId(),
                            status,
                            pageable
                    )
                    .map(OrderMapper::toDTO);
        }

        // CAS 4 — no filter
        else {
            orders = orderService
                    .findByUserId(user.getId(), pageable)
                    .map(OrderMapper::toDTO);
        }

        return ResponseEntity.ok(orders);
    }

    /**
     * Récupérer une commande par ID
     * Sécurité externalisée (OrderSecurity)
     */
    @PreAuthorize("@orderSecurity.isOwnerOrAdmin(#id, authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getById(@PathVariable Long id) {

        Order order = orderService.findById(id);

        return ResponseEntity.ok(OrderMapper.toDTO(order));
    }

    /**
     * Annuler une commande (OWNER ou ADMIN)
     */
    @PreAuthorize("@orderSecurity.isOwnerOrAdmin(#id, authentication)")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {

        orderService.cancelOrder(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Mapper DTO -> Entity
     * On map uniquement les champs nécessaires
     */
    private Order mapToOrder(OrderRequestDTO req) {

        List<OrderItem> items = req.items().stream()
                .map(item -> OrderItem.builder()
                        .product(Product.builder()
                                .id(item.productId()) // seulement ID
                                .build())
                        .quantity(item.quantity())
                        .build())
                .toList();

        return Order.builder()
                .shippingAddress(req.shippingAddress())
                .items(items)
                .build();
    }
}