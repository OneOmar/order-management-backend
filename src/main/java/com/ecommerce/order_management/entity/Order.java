package com.ecommerce.order_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Order contenant la liste d'order items, total, status, user.
 */
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Utilisateur requis pour la commande")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Size(min = 1, message = "La commande doit contenir au moins 1 article")
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @NotNull(message = "Le montant total est requis")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le total doit être positif ou nul")
    private BigDecimal totalAmount;

    @NotBlank(message = "Adresse de livraison requise")
    private String shippingAddress;

    @CreationTimestamp
    private LocalDateTime createdAt;
}