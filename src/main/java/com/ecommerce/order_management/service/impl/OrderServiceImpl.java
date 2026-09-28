package com.ecommerce.order_management.service.impl;

import com.ecommerce.order_management.entity.*;
import com.ecommerce.order_management.exception.InsufficientStockException;
import com.ecommerce.order_management.exception.NotFoundException;
import com.ecommerce.order_management.repository.OrderRepository;
import com.ecommerce.order_management.service.OrderService;
import com.ecommerce.order_management.service.ProductService;
import com.ecommerce.order_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implémentation transactionnelle de OrderService.
 * - createOrder : vérifie le stock, décrémente, calcule total, persiste.
 * - cancelOrder : annule et restocke les items si applicable.
 * pour concurrence élevée, ajouter locking / optimistic @Version.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;

    /**
     * Créer une commande :
     * - vérifie stock
     * - calcule total
     * - décrémente stock
     */
    @Override
    @Transactional
    public Order createOrder(Long userId, Order orderRequest) {

        // 1. récupérer user
        User user = userService.findById(userId);
        orderRequest.setUser(user);

        // 2. valider items
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("La commande doit contenir au moins un article");
        }

        BigDecimal total = BigDecimal.ZERO;

        // 3. traiter chaque item
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

            // check stock
            if (product.getStock() < qty) {
                throw new InsufficientStockException("Stock insuffisant pour produit id=" + productId);
            }

            // set données sécurisées
            item.setProduct(product);
            item.setUnitPrice(product.getPrice());
            item.setOrder(orderRequest);

            // décrémenter stock
            productService.decreaseStock(productId, qty);

            // calcul total
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(lineTotal);
        }

        // 4. set order fields
        orderRequest.setTotalAmount(total);
        orderRequest.setStatus(OrderStatus.PENDING);

        // 5. save (cascade pour items)
        return orderRepository.save(orderRequest);
    }

    /**
     * Récupérer une commande
     */
    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commande non trouvée id=" + id));
    }

    /**
     * Commandes d’un user
     */
    @Override
    public List<Order> findByUserId(Long userId) {
        User user = userService.findById(userId);
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Commandes par statut
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
     * - restock produits
     */
    @Override
    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = findById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) return;

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Impossible d'annuler une commande livrée");
        }

        // restock
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