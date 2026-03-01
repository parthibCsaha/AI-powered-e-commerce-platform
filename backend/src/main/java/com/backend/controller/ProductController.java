package com.backend.controller;

import com.backend.dto.ProductResponse;
import com.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductResponse> getProducts() {
        return productService.getProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @GetMapping("/search")
    public List<ProductResponse> filterSearch(@RequestParam(required = false) String name,
                                              @RequestParam(required = false) String brand,
                                              @RequestParam(required = false) Double minPrice,
                                              @RequestParam(required = false) Double maxPrice) {

        return productService.filterSearch(name, brand, minPrice, maxPrice);
    }

}
