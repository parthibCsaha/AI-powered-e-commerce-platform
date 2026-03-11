package com.backend.repository;

import com.backend.entity.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {


    Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);

}
