package com.backend.dto;

public record FilterSearchRequest(

        String name,
        String brand,
        Double maxPrice,
        Double minPrice

) {
}
