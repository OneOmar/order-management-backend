package com.ecommerce.order_management.service.impl;

import com.ecommerce.order_management.entity.Product;
import com.ecommerce.order_management.exception.NotFoundException;
import com.ecommerce.order_management.repository.ProductRepository;
import com.ecommerce.order_management.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation basique du ProductService.
 * - lecture en readOnly par défaut
 * - opérations d'écriture marquées @Transactional
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Page<Product> listActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    @Override
    public Page<Product> listByCategory(String category, Pageable pageable) {
        return productRepository.findByCategoryAndActiveTrue(category, pageable);
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produit non trouvé id=" + id));
    }

    @Override
    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    /**
     * Décrémente le stock d'un produit de façon transactionnelle.
     * Lance une RuntimeException (InsufficientStockException) si stock insuffisant.
     * Ici on garde logique simple ; pour concurrence élevée, ajouter @Version sur Product.
     */
    @Override
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Produit non trouvé id=" + productId));

        int current = product.getStock() == null ? 0 : product.getStock();
        if (current < quantity) {
            throw new IllegalArgumentException("Stock insuffisant pour produit id=" + productId);
        }
        product.setStock(current - quantity);
        productRepository.save(product);
    }

    @Override
    public List<Product> findLowStock(int threshold) {
        return productRepository.findByStockLessThan(threshold);
    }
}