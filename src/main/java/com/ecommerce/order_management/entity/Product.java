package com.ecommerce.order_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité Product : nom, description, price, stock, category, active.
 * Ajout de validations sur champs critiques.
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Le nom du produit est requis")
    private String name;

    @Column(length = 1000)
    @Size(max = 1000, message = "Description trop longue")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Le prix est requis")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
    private BigDecimal price;

    @Column(nullable = false)
    @NotNull(message = "Stock requis")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stock;

    private String imageUrl;

    @NotBlank(message = "La catégorie est requise")
    private String category;

    private boolean active = true;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}