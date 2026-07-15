package com.project.e_commerce.infrastructure.persistence.category;

import com.project.e_commerce.domain.Category;
import com.project.e_commerce.infrastructure.persistence.product.ProductMapper;

import java.util.ArrayList;
import java.util.List;

public class CategoryMapper {

    public static Category toDomain(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Category(
                entity.getCategoryId(),
                entity.getCategoryName(),
                entity.getCategoryDescription(),
                ProductMapper.toDomainShallowList(entity.getProducts())
        );
    }

    public static Category toDomainShallow(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Category(
                entity.getCategoryId(),
                entity.getCategoryName(),
                entity.getCategoryDescription(),
                List.of()
        );
    }

    public static CategoryEntity toEntity(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryEntity(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCategoryDescription(),
                ProductMapper.toEntityShallowList(category.getProducts())
        );
    }

    public static CategoryEntity toEntityShallow(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryEntity(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCategoryDescription(),
                new ArrayList<>()
        );
    }

    public static List<Category> toDomainList(List<CategoryEntity> entities) {
        return entities.stream().map(CategoryMapper::toDomain).toList();
    }

    public static List<Category> toDomainShallowList(List<CategoryEntity> entities) {
        return entities.stream().map(CategoryMapper::toDomainShallow).toList();
    }

    public static List<CategoryEntity> toEntityList(List<Category> categories) {
        return new ArrayList<>(categories.stream().map(CategoryMapper::toEntity).toList());
    }

    public static List<CategoryEntity> toEntityShallowList(List<Category> categories) {
        return new ArrayList<>(categories.stream().map(CategoryMapper::toEntityShallow).toList());
    }
}
