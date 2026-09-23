package com.ecommerce.order_management.service;

import com.ecommerce.order_management.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contrat pour la couche service Produit.
 * - fournit pagination pour catalogue actif
 * - opérations CRUD basiques
 * - helper pour gestion de stock
 */
public interface ProductService {

    Page<Product> listActive(Pageable pageable);

    Page<Product> listByCategory(String category, Pageable pageable);

    Product findById(Long id);

    Product save(Product product);

    /**
     * Décrémente le stock pour un produit donné.
     * Implémentation doit vérifier la disponibilité et lancer une exception si insuffisant.
     */
    void decreaseStock(Long productId, int quantity);

    List<Product> findLowStock(int threshold);
}