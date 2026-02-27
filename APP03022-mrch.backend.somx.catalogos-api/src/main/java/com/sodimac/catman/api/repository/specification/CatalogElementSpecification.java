package com.sodimac.catman.api.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.sodimac.catman.api.model.entity.CatalogDetail;

import jakarta.persistence.criteria.Predicate;

public class CatalogElementSpecification {

    private CatalogElementSpecification() {
    }

    /**
     *
     * @param headerId        
     * @param elementId      
     * @param element          
     * @param value            
     * @param parentCatalogId  
     * @param parentElementId  
     * @param status           
     * @return 
     */
    public static Specification<CatalogDetail> withFilters(
            Integer headerId,
            Integer elementId,
            String element,
            String value,
            Integer parentCatalogId,
            Integer parentElementId,
            Integer status,
            String key) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (headerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("header").get("id"), headerId));
            }

            if (elementId != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), elementId));
            }

            if (element != null && !element.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("key")),
                        "%" + element.toLowerCase() + "%"));
            }

            if (value != null && !value.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("value")),
                        "%" + value.toLowerCase() + "%"));
            }

            if (parentCatalogId != null) {
                predicates.add(criteriaBuilder.equal(root.get("parentCatalogId"), parentCatalogId));
            }

            if (parentElementId != null) {
                predicates.add(criteriaBuilder.equal(root.get("parentElementId"), parentElementId));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (key != null && !key.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("key")),
                        "%" + key.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}







