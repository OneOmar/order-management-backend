package com.ecommerce.order_management.service.impl;

import com.ecommerce.order_management.entity.*;
import com.ecommerce.order_management.exception.InsufficientStockException;
import com.ecommerce.order_management.exception.NotFoundException;
import com.ecommerce.order_management.repository.OrderRepository;
import com.ecommerce.order_management.repository.OrderItemRepository;
import com.ecommerce.order_management.service.OrderService;
import com.ecommerce.order_management.service.ProductService;
import com.ecommerce.order_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final UserService userService;

    @Override
    @Transactional
    public Order createOrder(Long userId, Order orderRequest) {
        // load user
        User user = userService.findById(userId);
        orderRequest.setUser(user);

        // validate items
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("La commande doit contenir au moins un article");
        }

        BigDecimal total = BigDecimal.ZERO;

        // For each item: load product, check stock, set unitPrice, link to order, decrement stock
        List<OrderItem> processedItems = orderRequest.getItems().stream().map(item -> {
            Long productId = item.getProduct() != null ? item.getProduct().getId() : null;
            if (productId == null) {
                throw new IllegalArgumentException("Produit absent dans un order item");
            }

            Product product = productService.findById(productId);

            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            if (product.getStock() < qty) {
                throw new InsufficientStockException("Stock insuffisant pour produit id=" + productId);
            }

            // set unit price from product current price
            item.setUnitPrice(product.getPrice());
            item.setOrder(orderRequest);
            item.setProduct(product);

            // decrement stock and persist via productService
            productService.decreaseStock(productId, qty);

            // accumulate total
            return item;
        }).collect(Collectors.toList());

        for (OrderItem itm : processedItems) {
            BigDecimal line = itm.getUnitPrice().multiply(BigDecimal.valueOf(itm.getQuantity()));
            total = total.add(line);
        }

        orderRequest.setItems(processedItems);
        orderRequest.setTotalAmount(total);
        orderRequest.setStatus(OrderStatus.PENDING);

        // save order (cascade saves items)
        Order saved = orderRepository.save(orderRequest);

        // ensure order items persisted (optional)
        if (saved.getItems() != null) {
            saved.getItems().forEach(orderItemRepository::save);
        }

        return saved;
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commande non trouvée id=" + id));
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        User user = userService.findById(userId);
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = findById(orderId);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = findById(orderId);
        // Only allow cancel if not already delivered/cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Impossible d'annuler une commande livrée");
        }

        // restock items
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                // add back the quantity to product stock
                Product product = productService.findById(item.getProduct().getId());
                int current = product.getStock() == null ? 0 : product.getStock();
                product.setStock(current + (item.getQuantity() == null ? 0 : item.getQuantity()));
                productService.save(product);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}