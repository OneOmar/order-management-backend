package com.ecommerce.order_management.service.impl;

import com.ecommerce.order_management.entity.*;
import com.ecommerce.order_management.exception.InsufficientStockException;
import com.ecommerce.order_management.exception.NotFoundException;
import com.ecommerce.order_management.repository.OrderRepository;
import com.ecommerce.order_management.service.OrderService;
import com.ecommerce.order_management.service.ProductService;
import com.ecommerce.order_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;

    /**
     * Créer une commande :
     * - valide les items
     * - vérifie le stock
     * - calcule le total
     * - décrémente le stock
     */
    @Override
    @Transactional
    public Order createOrder(Long userId, Order orderRequest) {

        // Associer l'utilisateur
        User user = userService.findById(userId);
        orderRequest.setUser(user);

        // Validation des items
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("La commande doit contenir au moins un article");
        }

        BigDecimal total = BigDecimal.ZERO;

        // Traitement de chaque item
        for (OrderItem item : orderRequest.getItems()) {

            Long productId = item.getProduct() != null ? item.getProduct().getId() : null;
            if (productId == null) {
                throw new IllegalArgumentException("Produit absent");
            }

            Product product = productService.findById(productId);

            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            if (qty <= 0) {
                throw new IllegalArgumentException("Quantité invalide");
            }

            // Vérifier stock
            if (product.getStock() < qty) {
                throw new InsufficientStockException("Stock insuffisant pour produit id=" + productId);
            }

            // Sécuriser les données côté serveur
            item.setProduct(product);
            item.setUnitPrice(product.getPrice());
            item.setOrder(orderRequest);

            // Décrémenter stock
            productService.decreaseStock(productId, qty);

            // Calcul total ligne
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(lineTotal);
        }

        // Finaliser commande
        orderRequest.setTotalAmount(total);
        orderRequest.setStatus(OrderStatus.PENDING);

        // Persistance (cascade items)
        return orderRepository.save(orderRequest);
    }

    /**
     * Récupérer une commande par ID
     */
    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commande non trouvée id=" + id));
    }

    /**
     * Commandes paginées d’un utilisateur
     */
    @Override
    public Page<Order> findByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    /**
     * Filtrer par statut
     */
    @Override
    public Page<Order> findByUserIdAndStatus(Long userId, String status, Pageable pageable) {

        // Convertir String → Enum
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Statut invalide: " + status);
        }

        return orderRepository.findByUserIdAndStatus(userId, orderStatus, pageable);
    }

    /**
     * Filtrer par intervalle de dates
     */
    @Override
    public Page<Order> findByUserIdAndDateRange(
            Long userId,
            String startDate,
            String endDate,
            Pageable pageable
    ) {

        LocalDateTime start;
        LocalDateTime end;

        try {
            // Début de journée / fin de journée
            start = LocalDate.parse(startDate).atStartOfDay();
            end = LocalDate.parse(endDate).atTime(23, 59, 59);
        } catch (Exception e) {
            throw new IllegalArgumentException("Format de date invalide (yyyy-MM-dd)");
        }

        // Validation métier
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("startDate doit être avant endDate");
        }

        return orderRepository.findByUserIdAndCreatedAtBetween(
                userId,
                start,
                end,
                pageable
        );
    }

    /**
     * Filtrer par statut + intervalle de dates
     */
    @Override
    public Page<Order> findByUserIdAndStatusAndDateRange(
            Long userId,
            String status,
            String startDate,
            String endDate,
            Pageable pageable
    ) {

        // Conversion status
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Statut invalide: " + status);
        }

        LocalDateTime start;
        LocalDateTime end;

        try {
            start = LocalDate.parse(startDate).atStartOfDay();
            end = LocalDate.parse(endDate).atTime(23, 59, 59);
        } catch (Exception e) {
            throw new IllegalArgumentException("Format de date invalide (yyyy-MM-dd)");
        }

        // Validation intervalle
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("startDate doit être avant endDate");
        }

        return orderRepository.findByUserIdAndStatusAndCreatedAtBetween(
                userId,
                orderStatus,
                start,
                end,
                pageable
        );
    }

    /**
     * Commandes par statut (non paginé)
     */
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Mise à jour du statut (ADMIN)
     */
    @Override
    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = findById(orderId);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Annuler une commande :
     * - interdit si DELIVERED
     * - restock les produits
     */
    @Override
    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = findById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) return;

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Impossible d'annuler une commande livrée");
        }

        // Restock produits
        for (OrderItem item : order.getItems()) {

            Product product = productService.findById(item.getProduct().getId());
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();

            product.setStock(product.getStock() + qty);
            productService.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}