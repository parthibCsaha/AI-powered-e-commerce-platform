package com.backend.dto;

public record ReviewRequest(
    String comment,
    Long productId
) {
} 
