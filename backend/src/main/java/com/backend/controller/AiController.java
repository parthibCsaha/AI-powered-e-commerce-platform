package com.backend.controller;

import com.backend.dto.AiSearchRequest;
import com.backend.entity.Product;
import com.backend.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    @PostMapping("/search")
    public List<Product> searchProducts(@RequestBody AiSearchRequest request) {
        return aiService.searchProducts(request.query());
    }

}
