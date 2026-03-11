package com.backend.dto;

import com.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        List<OrderItemResponse> items,
        BigDecimal totalPrice,
        OrderStatus status,
        LocalDateTime createdAt
) {
}
