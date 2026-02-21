package com.backend.dto;

import java.math.BigDecimal;

public record ProductResponse(
        String name,
        String brand,
        String description,
        Double price,
        BigDecimal stock,
        Double rating
) {
}
