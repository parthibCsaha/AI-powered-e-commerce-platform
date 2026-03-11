package com.backend.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        String brandName,
        String description,
        BigDecimal price,
        BigDecimal quantity
) {
}
