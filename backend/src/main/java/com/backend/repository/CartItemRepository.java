package com.backend.repository;

import com.backend.entity.Cart;
import com.backend.entity.CartItem;
import com.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Page<CartItem> findByCart(Cart cart, Pageable pageable);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

}

