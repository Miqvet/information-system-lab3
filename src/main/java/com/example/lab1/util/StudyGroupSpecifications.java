package com.example.lab1.util;

import com.example.lab1.entity.StudyGroup;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class StudyGroupSpecifications {
    private StudyGroupSpecifications() {}

    public static Specification<StudyGroup> filterByStringField(String fieldName, String fieldValue) {
        return (root, query, criteriaBuilder) -> {
            try {
                Path<?> field = fieldName.contains(".") 
                    ? getNestedField(root, fieldName)
                    : root.get(fieldName);
                    
                return createPredicate(criteriaBuilder, field, fieldValue);
            } catch (IllegalArgumentException e) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(false));
            }
        };
    }
    private static Path<?> getNestedField(Path<?> root, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Path<?> currentPath = root;
        
        for (int i = 0; i < parts.length - 1; i++) {
            currentPath = currentPath.get(parts[i]);
        }
        return currentPath.get(parts[parts.length - 1]);
    }

    private static Predicate createPredicate(
            CriteriaBuilder criteriaBuilder, Path<?> field, String fieldValue) {
        Class<?> fieldType = field.getJavaType();
        
        if (fieldType == String.class) {
            return criteriaBuilder.equal(field, fieldValue);
        }
        if (fieldType == Integer.class || fieldType == int.class) {
            try {
                return criteriaBuilder.equal(field, Integer.parseInt(fieldValue));
            } catch (NumberFormatException e) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(false));
            }
        }
        return criteriaBuilder.isTrue(criteriaBuilder.literal(false));
    }
}