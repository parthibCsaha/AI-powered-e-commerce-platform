package com.backend.dto;

import java.math.BigDecimal;

public record CartRequest(
        Long productId,
        BigDecimal quantity
) {
}
