package com.backend.repository;

import com.backend.entity.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {


    Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);


    @Query("""
            SELECT COUNT(oi) > 0
            FROM OrderItem oi
            WHERE oi.product.id = :productId
            AND oi.order.user.username = :username
        """)        
    boolean existsUserPurchase(Long productId, String username);

}
