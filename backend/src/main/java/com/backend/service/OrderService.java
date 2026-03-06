package com.backend.service;

import com.backend.dto.BuyProductRequest;
import com.backend.entity.Order;
import com.backend.entity.OrderItem;
import com.backend.entity.Product;
import com.backend.entity.User;
import com.backend.exception.BadRequestException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.OrderRepository;
import com.backend.repository.ProductRepository;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    public String buyProducts(BuyProductRequest buyProductRequest, String username) {

        Product product = productRepository.findById(buyProductRequest.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStock().intValue() < buyProductRequest.quantity().intValue()) {
            throw new BadRequestException("Not enough stock");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));



        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(buyProductRequest.quantity());
        orderItem.setPrice(product.getPrice());

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(buyProductRequest.quantity().doubleValue() * product.getPrice());
        order.getItems().add(orderItem);

        orderItem.setOrder(order);

        user.getOrders().add(order);

        product.setStock(product.getStock().subtract(buyProductRequest.quantity()));

        orderRepository.save(order);

        return "Order placed successfully.";

    }

}
