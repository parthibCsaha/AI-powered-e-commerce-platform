package com.backend.service;

import com.backend.dto.PageResponse;
import com.backend.dto.ProductRequest;
import com.backend.dto.ProductResponse;
import com.backend.entity.Product;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public PageResponse<ProductResponse> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        Page<ProductResponse> productResponses = products.map(this::mapToProductResponse);
        return PageResponse.form(productResponses);
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    public PageResponse<ProductResponse> filterSearch(String name, String brand, Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByFilters(
                name,
                brand,
                minPrice,
                maxPrice,
                pageable
        );

        Page<ProductResponse> productResponses = products.map(this::mapToProductResponse);
        return PageResponse.form(productResponses);
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse addProduct(ProductRequest productRequest) {
        Product product = new Product();
        product.setName(productRequest.name());
        product.setBrand(productRequest.brand());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());
        product.setStock(productRequest.stock());
        product.setRating(productRequest.rating());

        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(productRequest.name());
        product.setBrand(productRequest.brand());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());
        product.setStock(productRequest.stock());
        product.setRating(productRequest.rating());

        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
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
