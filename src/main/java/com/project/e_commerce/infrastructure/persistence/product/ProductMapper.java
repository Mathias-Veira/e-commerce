package com.project.e_commerce.infrastructure.persistence.product;

import com.project.e_commerce.domain.Product;
import com.project.e_commerce.infrastructure.persistence.category.CategoryMapper;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class ProductMapper {

    public static Product toDomain(ProductEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Product(
                entity.getProductId(),
                entity.getProductName(),
                CategoryMapper.toDomainShallowList(entity.getCategories()),
                entity.getProductPrice(),
                entity.getProductDescription(),
                entity.getProductStock(),
                entity.isDiscontinued()
        );
    }

    public static Product toDomainShallow(ProductEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Product(
                entity.getProductId(),
                entity.getProductName(),
                List.of(),
                entity.getProductPrice(),
                entity.getProductDescription(),
                entity.getProductStock(),
                entity.isDiscontinued()
        );
    }

    public static ProductEntity toEntity(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductEntity(
                product.getProductId(),
                product.getProductName(),
                CategoryMapper.toEntityShallowList(product.getCategories()),
                product.getProductPrice(),
                product.getProductDescription(),
                product.getProductStock(),
                product.isDiscontinued()
        );
    }

    public static ProductEntity toEntityShallow(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductEntity(
                product.getProductId(),
                product.getProductName(),
                new ArrayList<>(),
                product.getProductPrice(),
                product.getProductDescription(),
                product.getProductStock(),
                product.isDiscontinued()
        );
    }

    public static List<Product> toDomainList(List<ProductEntity> entities) {
        return entities.stream().map(ProductMapper::toDomain).toList();
    }

    public static List<Product> toDomainShallowList(List<ProductEntity> entities) {
        return entities.stream().map(ProductMapper::toDomainShallow).toList();
    }

    public static List<ProductEntity> toEntityList(List<Product> products) {
        return new ArrayList<>(products.stream().map(ProductMapper::toEntity).toList());
    }

    public static List<ProductEntity> toEntityShallowList(List<Product> products) {
        return new ArrayList<>(products.stream().map(ProductMapper::toEntityShallow).toList());
    }

    public static Page<Product> toDomainPage(Page<ProductEntity> entities) {
        return entities.map(ProductMapper::toDomain);
    }

    public static Page<Product> toDomainShallowPage(Page<ProductEntity> entities) {
        return entities.map(ProductMapper::toDomainShallow);
    }
}
