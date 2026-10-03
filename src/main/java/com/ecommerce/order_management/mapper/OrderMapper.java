package com.ecommerce.order_management.mapper;

import com.ecommerce.order_management.dto.response.OrderItemDTO;
import com.ecommerce.order_management.dto.response.OrderResponseDTO;
import com.ecommerce.order_management.entity.Order;
import com.ecommerce.order_management.entity.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

  public static OrderResponseDTO toDTO(Order order) {

    return new OrderResponseDTO(
            order.getId(),
            order.getUser().getEmail(),
            order.getTotalAmount(),
            order.getStatus().name(),
            order.getShippingAddress(),
            order.getCreatedAt(),
            mapItems(order.getItems())
    );
  }

  private static List<OrderItemDTO> mapItems(List<OrderItem> items) {
    return items.stream()
            .map(OrderMapper::mapItem)
            .collect(Collectors.toList());
  }

  private static OrderItemDTO mapItem(OrderItem item) {
    return new OrderItemDTO(
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getQuantity(),
            item.getUnitPrice()
    );
  }
}