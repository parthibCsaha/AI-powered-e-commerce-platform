package com.backend.service;

import com.backend.dto.BuyProductRequest;
import com.backend.dto.OrderResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    public List<OrderResponse> getAllOrders(String username, Pageable pageable) {

        Page<Order> orders = orderRepository.findByUserUsernameWithItems(username, pageable);

        List<OrderResponse> orderResponses = new ArrayList<>();

        for (Order order : orders.getContent()) {
            for (OrderItem item : order.getItems()) {
                OrderResponse orderResponse = new OrderResponse(
                    item.getProduct().getName(),
                    item.getProduct().getBrand(),
                    item.getProduct().getDescription(),
                    item.getPrice(),
                    item.getQuantity(),
                    item.getQuantity().doubleValue() * item.getPrice()
                );
                orderResponses.add(orderResponse);
            }
        }

        return orderResponses;
    }

    @Transactional
    public String buyProducts(BuyProductRequest buyProductRequest, String username) {

        if (buyProductRequest.quantity() == null || buyProductRequest.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }

        Product product = productRepository.findById(buyProductRequest.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStock().compareTo(buyProductRequest.quantity()) < 0) {
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

        product.setStock(product.getStock().subtract(buyProductRequest.quantity()));

        productRepository.save(product);
        orderRepository.save(order);

        return "Order placed successfully.";

    }
}
