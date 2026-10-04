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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * Créer une commande pour l'utilisateur connecté
     */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(
            Authentication authentication,
            @Valid @RequestBody OrderRequestDTO request
    ) {
        // récupérer user connecté
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        // mapper DTO -> entity
        Order order = mapToOrder(request);

        // créer commande
        Order saved = orderService.createOrder(user.getId(), order);

        // retourner réponse (201 CREATED)
        return ResponseEntity.status(201).body(OrderMapper.toDTO(saved));
    }

    /**
     * Récupérer les commandes du user avec pagination + filtres optionnels
     */
    @GetMapping("/me")
    public ResponseEntity<Page<OrderResponseDTO>> myOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        // récupérer user connecté
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        // pagination + tri par date desc
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<OrderResponseDTO> orders;

        // CAS 1 : filtre complet (status + date range)
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

        // CAS 2 : filtre date uniquement
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

        // CAS 3 : filtre status uniquement
        else if (status != null) {

            orders = orderService
                    .findByUserIdAndStatus(user.getId(), status, pageable)
                    .map(OrderMapper::toDTO);
        }

        // CAS 4 : aucun filtre
        else {

            orders = orderService
                    .findByUserId(user.getId(), pageable)
                    .map(OrderMapper::toDTO);
        }

        return ResponseEntity.ok(orders);
    }

    /**
     * Récupérer une commande par ID (OWNER ou ADMIN)
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Order order = orderService.findById(id);

        // check accès
        checkAccess(order, authentication);

        return ResponseEntity.ok(OrderMapper.toDTO(order));
    }

    /**
     * Annuler une commande (OWNER ou ADMIN)
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Order order = orderService.findById(id);

        // check accès
        checkAccess(order, authentication);

        orderService.cancelOrder(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Vérifie que l'utilisateur est OWNER ou ADMIN
     */
    private void checkAccess(Order order, Authentication auth) {
        String email = auth.getName();
        User user = userService.findByEmail(email);

        boolean isOwner = order.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ROLE_ADMIN");

        if (!(isOwner || isAdmin)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    /**
     * Convertir un OrderRequestDTO -> Order (entity)
     */
    private Order mapToOrder(OrderRequestDTO req) {

        // mapper items DTO -> entity
        List<OrderItem> items = req.items().stream()
                .map(item -> OrderItem.builder()
                        .product(Product.builder()
                                .id(item.productId()) // seulement ID
                                .build())
                        .quantity(item.quantity())
                        .build())
                .toList();

        // construire order
        return Order.builder()
                .shippingAddress(req.shippingAddress())
                .items(items)
                .build();
    }
}