package com.backend.specification;

import com.backend.dto.ProductSearchFilter;
import com.backend.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

        public static Specification<Product> fromFilter(ProductSearchFilter filter) {
                return (root, query, cb) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        // Brand — partial, case-insensitive match
                        if (filter.brand() != null && !filter.brand().isBlank()) {
                                predicates.add(
                                        cb.like(
                                                cb.lower(root.get("brand")),
                                                "%" + filter.brand().toLowerCase() + "%"
                                        )
                                );
                        }

                        // Product name — partial, case-insensitive match
                        if (filter.productName() != null && !filter.productName().isBlank()) {
                                predicates.add(
                                        cb.like(
                                                cb.lower(root.get("name")),
                                                "%" + filter.productName().toLowerCase() + "%"
                                        )
                                );
                        }

                        // Price range
                        if (filter.minPrice() != null) {
                                predicates.add(
                                        cb.greaterThanOrEqualTo(
                                                root.get("price"), filter.minPrice()
                                        )
                                );
                        }
                        if (filter.maxPrice() != null) {
                                predicates.add(
                                        cb.lessThanOrEqualTo(
                                                root.get("price"), filter.maxPrice()
                                        )
                                );
                        }

                        // Minimum rating
                        if (filter.minRating() != null) {
                                predicates.add(
                                        cb.greaterThanOrEqualTo(
                                                root.get("rating"), filter.minRating()
                                        )
                                );
                        }

                        // Keywords — match against name OR description, using OR between keywords
                        if (filter.keywords() != null && !filter.keywords().isEmpty()) {
                                List<Predicate> keywordPredicates = new ArrayList<>();
                                for (String keyword : filter.keywords()) {
                                        if (keyword == null || keyword.isBlank())
                                                continue;
                                        String pattern = "%" + keyword.toLowerCase() + "%";
                                        keywordPredicates.add(
                                                        cb.or(
                                                                        cb.like(cb.lower(root.get("name")), pattern),
                                                                        cb.like(cb.lower(root.get("description")),
                                                                                        pattern)));
                                }
                                if (!keywordPredicates.isEmpty()) {
                                        // OR between keywords — match products containing ANY keyword
                                        predicates.add(cb.or(keywordPredicates.toArray(new Predicate[0])));
                                }
                        }

                        return predicates.isEmpty()
                                        ? cb.conjunction()
                                        : cb.and(predicates.toArray(new Predicate[0]));
                };
        }
}
