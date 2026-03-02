package com.backend.dto;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String brand,
        String description,
        Double price,
        BigDecimal stock,
        Double rating
) {
}
