package com.backend.dto;

import com.backend.entity.Product;

import java.util.List;

public record AiSearchResponse(
        List<Product> products,
        ProductSearchFilter appliedFilters,
        String message) {
}
