package com.backend.dto;

import java.util.List;

public record ProductSearchFilter(
                String brand,
                String productName,
                Double minPrice,
                Double maxPrice,
                Double minRating,
                List<String> keywords) {
}
