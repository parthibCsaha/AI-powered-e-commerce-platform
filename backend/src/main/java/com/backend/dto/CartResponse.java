package com.backend.dto;

import java.math.BigDecimal;

public record CartResponse(
        Long id,
        Long productId,
        String productName,
        String productDescription,
        String productBrand,
        Double productPrice,
        BigDecimal quantity
) {
}  
