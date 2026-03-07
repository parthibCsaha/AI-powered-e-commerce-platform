package com.backend.dto;

import java.math.BigDecimal;

public record OrderResponse(
        String productName,
        String brandName,
        String description,
        Double price,
        BigDecimal quantity,
        Double totalPrice
) {
}
