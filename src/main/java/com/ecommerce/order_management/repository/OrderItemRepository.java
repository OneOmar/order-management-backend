package com.ecommerce.order_management.repository;

import com.ecommerce.order_management.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // repository basique pour OrderItem — méthodes custom à ajouter si besoin
}