package com.backend.dto;

import java.util.List;

public record ProductSearchFilter(
                String brand,
                String productName,
                String category,
                Double minPrice,
                Double maxPrice,
                Double minRating,
                List<String> keywords) {
}
