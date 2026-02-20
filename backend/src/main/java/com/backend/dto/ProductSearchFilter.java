package com.backend.dto;

import java.util.List;

public record ProductSearchFilter(
        String category,
        String color,
        Double minPrice,
        Double maxPrice,
        List<String> keywords
) {
}
