package com.backend.controller;

import com.backend.dto.AiSearchRequest;
import com.backend.dto.AiSearchResponse;
import com.backend.entity.Product;
import com.backend.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    @PostMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @Valid @RequestBody AiSearchRequest request) {
        log.info("AI search request: \"{}\"", request.query());
        AiSearchResponse response = aiService.aiSearchProducts(request.query());
        return ResponseEntity.ok(response.products());
    }



}
