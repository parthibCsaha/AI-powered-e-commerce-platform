package com.backend.service;

import com.backend.dto.CartResponse;
import com.backend.entity.Cart;
import com.backend.entity.CartItem;
import com.backend.entity.Product;
import com.backend.entity.User;
import com.backend.exception.BadRequestException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.CartItemRepository;
import com.backend.repository.CartRepository;
import com.backend.repository.ProductRepository;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    @Transactional(readOnly = true)
    public Page<CartResponse> getAllCart(String name, Pageable pageable) {
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = user.getCart();
        if (cart == null) {
            return Page.empty(pageable);
        }

        Page<CartItem> cartItems = cartItemRepository.findByCart(cart, pageable);

        return cartItems.map(item -> {
            Product product = item.getProduct();
            return new CartResponse(
                    item.getId(),
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getBrand(),
                    product.getPrice(),
                    item.getQuantity()
            );
        });
    }

    @Transactional
    public String addToCart(Long productId, BigDecimal quantity, String username) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            user.setCart(cart);
            cartRepository.save(cart);
        }

        // Check if product already exists in cart — increment instead of duplicating
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        BigDecimal newQuantity = (cartItem != null)
                ? cartItem.getQuantity().add(quantity)
                : quantity;

        // Validate stock
        if (product.getStock().compareTo(newQuantity) < 0) {
            throw new BadRequestException(
                    "Not enough stock for " + product.getName()
                    + ". Available: " + product.getStock()
                    + ", Requested: " + newQuantity
            );
        }

        if (cartItem != null) {
            cartItem.setQuantity(newQuantity);
        } 
        else {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setCart(cart);
            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);

        return "Product added to cart successfully.";
    }
}
