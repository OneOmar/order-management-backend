package com.ecommerce.order_management.repository;

import com.ecommerce.order_management.entity.Order;
import com.ecommerce.order_management.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    Page<Order> findByUserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<Order> findByUserIdAndStatusAndCreatedAtBetween(
            Long userId,
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

}