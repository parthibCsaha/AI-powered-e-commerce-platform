package com.backend.dto;

import java.math.BigDecimal;

public record BuyProductRequest(
        Long productId,
        BigDecimal quantity
) {
}
