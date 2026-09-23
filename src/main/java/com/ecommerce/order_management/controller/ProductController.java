package com.ecommerce.order_management.controller;

import com.ecommerce.order_management.entity.Product;
import com.ecommerce.order_management.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * API REST pour les produits.
 *
 * Endpoints:
 *  - GET  /api/products                : pagination des produits actifs
 *  - GET  /api/products/{id}           : récupérer un produit
 *  - GET  /api/products/category/{cat} : produits par catégorie (paged)
 *  - GET  /api/products/low-stock      : produits dont le stock < threshold
 *  - POST /api/products                : créer (ROLE_ADMIN recommandé)
 *  - PUT  /api/products/{id}           : mise à jour (ROLE_ADMIN recommandé)
 *  - DELETE /api/products/{id}         : soft-delete (désactivation) (ROLE_ADMIN recommandé)
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Liste paginée des produits actifs.
     * Query params : page (0), size (10)
     */
    @GetMapping
    public ResponseEntity<Page<Product>> listActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> result = productService.listActive(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Récupérer un produit par id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Lister par catégorie (paged).
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<Product>> listByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> result = productService.listByCategory(category, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Produits en faible stock.
     * Ex: /api/products/low-stock?threshold=5
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> lowStock(@RequestParam(defaultValue = "5") int threshold) {
        List<Product> products = productService.findLowStock(threshold);
        return ResponseEntity.ok(products);
    }

    /**
     * Créer un produit.
     * Sécurisé : seul ROLE_ADMIN peut créer (si activé dans SecurityConfig).
     * Retourne 201 Created + Location header.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Product> create(@Valid @RequestBody Product product) {
        Product saved = productService.save(product);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    /**
     * Mettre à jour un produit existant.
     * Partial update simple : on réécrit tout l'objet product côté save().
     * Sécurisé : ROLE_ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Product> update(@PathVariable Long id, @Valid @RequestBody Product product) {
        Product existing = productService.findById(id);
        // Map simple : remplacer champs principaux
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setImageUrl(product.getImageUrl());
        existing.setCategory(product.getCategory());
        existing.setActive(product.isActive());

        Product saved = productService.save(existing);
        return ResponseEntity.ok(saved);
    }

    /**
     * Suppression logique (soft-delete) : on marque active = false.
     * Sécurisé : ROLE_ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Product existing = productService.findById(id);
        existing.setActive(false);
        productService.save(existing);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}