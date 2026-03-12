package com.backend.service;

import com.backend.dto.PageResponse;
import com.backend.dto.ProductRequest;
import com.backend.dto.ProductResponse;
import com.backend.dto.ReviewRequest;
import com.backend.entity.OrderItem;
import com.backend.entity.Product;
import com.backend.entity.Review;
import com.backend.entity.User;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.OrderItemRepository;
import com.backend.repository.ProductRepository;
import com.backend.repository.ReviewRepository;
import com.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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

    private final OrderItemRepository orderItemRepository;

    private final ReviewRepository reviewRepository;

    private final UserRepository userRepository;

    // ===================== get all products with pagination and sorting =====================

    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public PageResponse<ProductResponse> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        Page<ProductResponse> productResponses = products.map(this::mapToProductResponse);
        return PageResponse.from(productResponses);
    }

    // ===================== get product by id =====================

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    // ===================== filter and search products =====================

    public PageResponse<ProductResponse> filterSearch(String name, String brand, Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByFilters(
                name,
                brand,
                minPrice,
                maxPrice,
                pageable
        );

        Page<ProductResponse> productResponses = products.map(this::mapToProductResponse);
        return PageResponse.from(productResponses);
    }

    // ===================== add product =====================

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

    // ===================== update product =====================

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

    // ===================== delete product =====================

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // ===================== helper method to map Product to ProductResponse =====================

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

    public String addReview(ReviewRequest reviewRequest, String name) {
        Product product = productRepository.findById(reviewRequest.productId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + reviewRequest.productId()));
        
        User user = userRepository.findByUsername(name)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + name));

        boolean flag = orderItemRepository.existsUserPurchase(product.getId(), name);

        if (!flag) {
            return "You can only review products you have purchased.";
        }

        Review review = new Review();
        review.setComment(reviewRequest.comment());
        review.setProduct(product);
        review.setUsername(name);
        review.setUser(user);
        reviewRepository.save(review);

        return "Your review has been added successfully.";

    }

}
