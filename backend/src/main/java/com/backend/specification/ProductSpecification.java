package com.backend.specification;

import com.backend.dto.ProductSearchFilter;
import com.backend.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class ProductSpecification {

    public static Specification<Product> fromFilter(ProductSearchFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.category() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("category").get("name")), filter.category()
                        )
                );
            }
            if (filter.color() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("color")), filter.color()
                        )
                );
            }
            if (filter.minPrice() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("price"), filter.minPrice()
                        )
                );
            }
            if (filter.maxPrice() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("price"), filter.maxPrice()
                        )
                );
            }
            if (filter.keywords() != null && !filter.keywords().isEmpty()) {
                List<Predicate> keywordPredicates = new ArrayList<>();
                for (String keyword : filter.keywords()) {
                    String pattern = "%" + keyword.toLowerCase() + "%";
                    keywordPredicates.add(
                            criteriaBuilder.or(
                                    criteriaBuilder.like(
                                            criteriaBuilder.lower(root.get("name")), pattern
                                    ),
                                    criteriaBuilder.like(
                                            criteriaBuilder.lower(root.get("description")), pattern
                                    )
                            )
                    );
                }
                predicates.add(criteriaBuilder.and(keywordPredicates.toArray(new Predicate[0])));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
