package com.backend.service;

import com.backend.entity.Cart;
import com.backend.entity.CartItem;
import com.backend.entity.Product;
import com.backend.entity.User;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.CartItemRepository;
import com.backend.repository.CartRepository;
import com.backend.repository.ProductRepository;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    public String getAllCart(String name) {
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = user.getCart();
        if (cart == null || cart.getItems().isEmpty()) {
            return "Cart is empty.";
        }

        StringBuilder response = new StringBuilder("Cart Items:\n");
        for (CartItem item : cart.getItems()) {
            response.append(String.format("Product: %s, Brand: %s, Quantity: %s\n", item.getProduct().getName(), item.getProduct().getBrand(), item.getQuantity()));
        }
        return response.toString();
    }

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
        }
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setCart(cart);
        cart.getItems().add(cartItem);

        cartRepository.save(cart);
        cartItemRepository.save(cartItem);

        return "Product added to cart successfully. " ;
    }
}
