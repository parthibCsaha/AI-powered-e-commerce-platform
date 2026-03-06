package com.backend.service;

import com.backend.entity.Cart;
import com.backend.entity.CartItem;
import com.backend.entity.Product;
import com.backend.entity.User;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.ProductRepository;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

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

        return "Product added to cart successfully.";
    }

}
