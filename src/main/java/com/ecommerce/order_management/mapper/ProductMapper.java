package com.ecommerce.order_management.mapper;

import com.ecommerce.order_management.dto.request.ProductRequestDTO;
import com.ecommerce.order_management.dto.response.ProductResponseDTO;
import com.ecommerce.order_management.entity.Product;

public class ProductMapper {

  public static ProductResponseDTO toDTO(Product product) {
    if (product == null) return null;

    return new ProductResponseDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getImageUrl(),
            product.getCategory(),
            product.isActive()
    );
  }

  public static Product toEntity(ProductRequestDTO dto) {
    if (dto == null) return null;

    return Product.builder()
            .name(dto.name())
            .description(dto.description())
            .price(dto.price())
            .stock(dto.stock())
            .imageUrl(dto.imageUrl())
            .category(dto.category())
            .active(dto.active() != null ? dto.active() : true)
            .build();
  }
}