package com.backend.servicetest;

import com.backend.dto.PageResponse;
import com.backend.dto.ProductRequest;
import com.backend.dto.ProductResponse;
import com.backend.entity.Product;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.ProductRepository;
import com.backend.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setBrand("Dell");
        product.setDescription("A powerful laptop");
        product.setPrice(999.99);
        product.setStock(BigDecimal.valueOf(50));
        product.setRating(4.5);

        productRequest = new ProductRequest("Laptop", "Dell", "A powerful laptop", 999.99, BigDecimal.valueOf(50), 4.5);
    }

    // ==================== getProducts ====================

    @Nested
    @DisplayName("getProducts")
    class GetProductsTests {

        @Test
        @DisplayName("should return paginated products")
        void shouldReturnPaginatedProducts() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            when(productRepository.findAll(pageable)).thenReturn(productPage);

            PageResponse<ProductResponse> result = productService.getProducts(pageable);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals("Laptop", result.content().get(0).name());
            assertEquals("Dell", result.content().get(0).brand());
            assertEquals(0, result.pageNumber());
            assertEquals(10, result.pageSize());
            assertEquals(1, result.totalElements());
            assertTrue(result.first());
            assertTrue(result.last());

            verify(productRepository, times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("should return empty page when no products exist")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(productRepository.findAll(pageable)).thenReturn(emptyPage);

            PageResponse<ProductResponse> result = productService.getProducts(pageable);

            assertNotNull(result);
            assertTrue(result.content().isEmpty());
            assertEquals(0, result.totalElements());

            verify(productRepository, times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("should return multiple products")
        void shouldReturnMultipleProducts() {
            Product product2 = new Product();
            product2.setId(2L);
            product2.setName("Phone");
            product2.setBrand("Samsung");
            product2.setDescription("A smart phone");
            product2.setPrice(699.99);
            product2.setStock(BigDecimal.valueOf(100));
            product2.setRating(4.2);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(product, product2), pageable, 2);

            when(productRepository.findAll(pageable)).thenReturn(productPage);

            PageResponse<ProductResponse> result = productService.getProducts(pageable);

            assertEquals(2, result.content().size());
            assertEquals("Laptop", result.content().get(0).name());
            assertEquals("Phone", result.content().get(1).name());
        }
    }

    // ==================== getProduct ====================

    @Nested
    @DisplayName("getProduct")
    class GetProductTests {

        @Test
        @DisplayName("should return product when found by id")
        void shouldReturnProductWhenFound() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            ProductResponse result = productService.getProduct(1L);

            assertNotNull(result);
            assertEquals("Laptop", result.name());
            assertEquals("Dell", result.brand());
            assertEquals("A powerful laptop", result.description());
            assertEquals(999.99, result.price());
            assertEquals(BigDecimal.valueOf(50), result.stock());
            assertEquals(4.5, result.rating());

            verify(productRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenProductNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> productService.getProduct(99L)
            );

            assertEquals("Product not found with id: 99", exception.getMessage());
            verify(productRepository, times(1)).findById(99L);
        }
    }

    // ==================== filterSearch ====================

    @Nested
    @DisplayName("filterSearch")
    class FilterSearchTests {

        @Test
        @DisplayName("should return filtered products by all criteria")
        void shouldReturnFilteredProducts() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            when(productRepository.findByFilters("Laptop", "Dell", 500.0, 1500.0, pageable))
                    .thenReturn(productPage);

            PageResponse<ProductResponse> result = productService.filterSearch("Laptop", "Dell", 500.0, 1500.0, pageable);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals("Laptop", result.content().get(0).name());

            verify(productRepository, times(1)).findByFilters("Laptop", "Dell", 500.0, 1500.0, pageable);
        }

        @Test
        @DisplayName("should return filtered products with null parameters")
        void shouldReturnFilteredProductsWithNullParams() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            when(productRepository.findByFilters(null, null, null, null, pageable))
                    .thenReturn(productPage);

            PageResponse<ProductResponse> result = productService.filterSearch(null, null, null, null, pageable);

            assertNotNull(result);
            assertEquals(1, result.content().size());

            verify(productRepository, times(1)).findByFilters(null, null, null, null, pageable);
        }

        @Test
        @DisplayName("should return empty results when no products match filter")
        void shouldReturnEmptyWhenNoMatch() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(productRepository.findByFilters("NonExistent", null, null, null, pageable))
                    .thenReturn(emptyPage);

            PageResponse<ProductResponse> result = productService.filterSearch("NonExistent", null, null, null, pageable);

            assertTrue(result.content().isEmpty());
            assertEquals(0, result.totalElements());
        }
    }

    // ==================== addProduct ====================

    @Nested
    @DisplayName("addProduct")
    class AddProductTests {

        @Test
        @DisplayName("should add product and return response")
        void shouldAddProduct() {
            when(productRepository.save(any(Product.class))).thenReturn(product);

            ProductResponse result = productService.addProduct(productRequest);

            assertNotNull(result);
            assertEquals("Laptop", result.name());
            assertEquals("Dell", result.brand());
            assertEquals("A powerful laptop", result.description());
            assertEquals(999.99, result.price());
            assertEquals(BigDecimal.valueOf(50), result.stock());
            assertEquals(4.5, result.rating());

            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("should correctly map request fields to entity")
        void shouldMapRequestFieldsToEntity() {
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                assertEquals("Laptop", saved.getName());
                assertEquals("Dell", saved.getBrand());
                assertEquals("A powerful laptop", saved.getDescription());
                assertEquals(999.99, saved.getPrice());
                assertEquals(BigDecimal.valueOf(50), saved.getStock());
                assertEquals(4.5, saved.getRating());
                saved.setId(1L);
                return saved;
            });

            productService.addProduct(productRequest);

            verify(productRepository).save(any(Product.class));
        }
    }

    // ==================== updateProduct ====================

    @Nested
    @DisplayName("updateProduct")
    class UpdateProductTests {

        @Test
        @DisplayName("should update product and return response")
        void shouldUpdateProduct() {
            ProductRequest updateRequest = new ProductRequest("Updated Laptop", "HP", "Updated desc", 1299.99, BigDecimal.valueOf(30), 4.8);

            Product updatedProduct = new Product();
            updatedProduct.setId(1L);
            updatedProduct.setName("Updated Laptop");
            updatedProduct.setBrand("HP");
            updatedProduct.setDescription("Updated desc");
            updatedProduct.setPrice(1299.99);
            updatedProduct.setStock(BigDecimal.valueOf(30));
            updatedProduct.setRating(4.8);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

            ProductResponse result = productService.updateProduct(1L, updateRequest);

            assertNotNull(result);
            assertEquals("Updated Laptop", result.name());
            assertEquals("HP", result.brand());
            assertEquals("Updated desc", result.description());
            assertEquals(1299.99, result.price());
            assertEquals(BigDecimal.valueOf(30), result.stock());
            assertEquals(4.8, result.rating());

            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when updating non-existent product")
        void shouldThrowWhenUpdatingNonExistentProduct() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> productService.updateProduct(99L, productRequest)
            );

            assertEquals("Product not found with id: 99", exception.getMessage());
            verify(productRepository, times(1)).findById(99L);
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    // ==================== deleteProduct ====================

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProductTests {

        @Test
        @DisplayName("should delete product when it exists")
        void shouldDeleteProduct() {
            when(productRepository.existsById(1L)).thenReturn(true);
            doNothing().when(productRepository).deleteById(1L);

            assertDoesNotThrow(() -> productService.deleteProduct(1L));

            verify(productRepository, times(1)).existsById(1L);
            verify(productRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when deleting non-existent product")
        void shouldThrowWhenDeletingNonExistentProduct() {
            when(productRepository.existsById(99L)).thenReturn(false);

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> productService.deleteProduct(99L)
            );

            assertEquals("Product not found with id: 99", exception.getMessage());
            verify(productRepository, times(1)).existsById(99L);
            verify(productRepository, never()).deleteById(anyLong());
        }
    }
}