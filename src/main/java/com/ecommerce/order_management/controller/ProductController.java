package com.ecommerce.order_management.controller;

import com.ecommerce.order_management.dto.request.ProductRequestDTO;
import com.ecommerce.order_management.dto.response.ProductResponseDTO;
import com.ecommerce.order_management.entity.Product;
import com.ecommerce.order_management.mapper.ProductMapper;
import com.ecommerce.order_management.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * API REST pour les produits.
 * <p>
 * Endpoints:
 * - GET  /api/products                : pagination des produits actifs
 * - GET  /api/products/{id}           : récupérer un produit
 * - GET  /api/products/category/{cat} : produits par catégorie (paged)
 * - GET  /api/products/low-stock      : produits dont le stock < threshold
 * - POST /api/products                : créer (ROLE_ADMIN recommandé)
 * - PUT  /api/products/{id}           : mise à jour (ROLE_ADMIN recommandé)
 * - DELETE /api/products/{id}         : soft-delete (désactivation) (ROLE_ADMIN recommandé)
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  /**
   * Liste paginée des produits actifs.
   * Query params : page (0), size (10)
   */
  @GetMapping
  public ResponseEntity<Page<ProductResponseDTO>> listActive(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);

    Page<ProductResponseDTO> result = productService
            .listActive(pageable)
            .map(ProductMapper::toDTO);

    return ResponseEntity.ok(result);
  }

  /**
   * Récupérer un produit par id.
   */
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponseDTO> getById(@PathVariable Long id) {
    Product product = productService.findById(id);
    return ResponseEntity.ok(ProductMapper.toDTO(product));
  }

  /**
   * Lister par catégorie (paged).
   */
  @GetMapping("/category/{category}")
  public ResponseEntity<Page<ProductResponseDTO>> listByCategory(
          @PathVariable String category,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);

    Page<ProductResponseDTO> result = productService
            .listByCategory(category, pageable)
            .map(ProductMapper::toDTO);

    return ResponseEntity.ok(result);
  }

  /**
   * Produits en faible stock.
   * Ex: /api/products/low-stock?threshold=5
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("/low-stock")
  public ResponseEntity<List<ProductResponseDTO>> lowStock(
          @RequestParam(defaultValue = "5") int threshold
  ) {
    List<ProductResponseDTO> products = productService
            .findLowStock(threshold)
            .stream()
            .map(ProductMapper::toDTO)
            .toList();

    return ResponseEntity.ok(products);
  }

  /**
   * Créer un produit.
   * Sécurisé : seul ROLE_ADMIN peut créer (si activé dans SecurityConfig).
   * Retourne 201 Created + Location header.
   */
  @PostMapping
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO dto) {

    Product product = ProductMapper.toEntity(dto);
    Product saved = productService.save(product);

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(saved.getId())
            .toUri();

    return ResponseEntity.created(location).body(ProductMapper.toDTO(saved));
  }

  /**
   * Mettre à jour un produit existant.
   * Partial update simple : on réécrit tout l'objet product côté save().
   * Sécurisé : ROLE_ADMIN.
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ProductResponseDTO> update(
          @PathVariable Long id,
          @Valid @RequestBody ProductRequestDTO dto
  ) {
    Product existing = productService.findById(id);

    // mapping DTO → entity (update champs)
    existing.setName(dto.name());
    existing.setDescription(dto.description());
    existing.setPrice(dto.price());
    existing.setStock(dto.stock());
    existing.setImageUrl(dto.imageUrl());
    existing.setCategory(dto.category());
    existing.setActive(dto.active() != null ? dto.active() : existing.isActive());


    Product saved = productService.save(existing);

    return ResponseEntity.ok(ProductMapper.toDTO(saved));
  }

  /**
   * Suppression logique (soft-delete) : on marque active = false.
   * Sécurisé : ROLE_ADMIN.
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {

    Product existing = productService.findById(id);

    if (!existing.isActive()) {
      return ResponseEntity.noContent().build();
    }

    existing.setActive(false);
    productService.save(existing);

    return ResponseEntity.noContent().build();
  }

}