package com.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AiSearchRequest(
        @NotBlank(message = "Search query must not be blank")
        String query
) {
}
