package com.backend.service;

import com.backend.dto.BuyProductRequest;
import com.backend.dto.OrderItemResponse;
import com.backend.dto.OrderResponse;
import com.backend.entity.*;
import com.backend.entity.OrderStatus;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(String username, Pageable pageable) {

        Page<Order> orders = orderRepository.findByUserUsernameWithItems(username, pageable);

        return orders.map(order -> {
            List<OrderItemResponse> itemResponses = order
                    .getItems()
                    .stream()
                    .map(item -> {
                        Product product = item.getProduct();
                        return new OrderItemResponse(
                                product.getId(),
                                product.getName(),
                                product.getBrand(),
                                product.getDescription(),
                                item.getPrice(),
                                item.getQuantity()
                        );
                    })
                    .toList();

            return new OrderResponse(
                    order.getId(),
                    itemResponses,
                    order.getTotalPrice(),
                    order.getStatus(),
                    order.getCreatedAt()
            );
        });
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
        orderItem.setPrice(BigDecimal.valueOf(product.getPrice()));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.valueOf(product.getPrice()).multiply(buyProductRequest.quantity()));
        order.getItems().add(orderItem);

        orderItem.setOrder(order);

        product.setStock(product.getStock().subtract(buyProductRequest.quantity()));

        productRepository.save(product);
        orderRepository.save(order);

        return "Order placed successfully.";
    }

    @Transactional
    public String checkoutCart(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = user.getCart();
        if (cart == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        List<CartItem> cartItems = cart.getItems();

        // Validate stock for ALL items before placing the order
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStock().compareTo(cartItem.getQuantity()) < 0) {
                throw new BadRequestException(
                        "Not enough stock for " + product.getName()
                        + ". Available: " + product.getStock()
                        + ", Requested: " + cartItem.getQuantity()
                );
            }
        }

        // Create order with all cart items
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(BigDecimal.valueOf(product.getPrice()));
            orderItem.setOrder(order);

            order.getItems().add(orderItem);

            totalPrice = totalPrice.add(BigDecimal.valueOf(product.getPrice()).multiply(cartItem.getQuantity()));

            // Deduct stock
            product.setStock(product.getStock().subtract(cartItem.getQuantity()));
            productRepository.save(product);
        }

        order.setTotalPrice(totalPrice);
        orderRepository.save(order);

        // Clear the cart after successful checkout
        cart.getItems().clear();

        return "Order placed successfully. " + cartItems.size() + " item(s) ordered.";
    }
}
