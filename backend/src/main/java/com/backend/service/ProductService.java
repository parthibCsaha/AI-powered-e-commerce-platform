package com.backend.service;

import com.backend.dto.ProductResponse;
import com.backend.entity.Product;
import com.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> getProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(new Function<Product, ProductResponse>() {
                    @Override
                    public ProductResponse apply(Product product) {
                        return mapToProductResponse(product);
                    }
                })
                .toList();
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    public List<ProductResponse> filterSearch(String name, String brand, Double minPrice, Double maxPrice) {
        List<Product> products = productRepository.findByFilters(name, brand, minPrice, maxPrice);

        log.info("Filtered products: {}", products);

        return products.stream()
                .map(this::mapToProductResponse)
                .toList();

    }

    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getRating()
        );
    }

}
