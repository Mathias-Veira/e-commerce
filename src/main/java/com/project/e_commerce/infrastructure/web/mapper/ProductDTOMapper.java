package com.project.e_commerce.infrastructure.web.mapper;

import com.project.e_commerce.domain.Product;
import com.project.e_commerce.infrastructure.web.dto.request.ProductDTORequest;
import com.project.e_commerce.infrastructure.web.dto.response.ProductDTOResponse;

import java.util.List;

public class ProductDTOMapper {

    public static Product toDomain(ProductDTORequest request) {
        if (request == null) {
            return null;
        }
        return new Product(
                request.getProductId(),
                request.getProductName(),
                CategoryDTOMapper.shallowList(request.getCategories()),
                request.getProductPrice(),
                request.getProductDescription(),
                request.getProductStock(),
                request.isDiscontinued()
        );
    }

    public static ProductDTOResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductDTOResponse(
                product.getProductId(),
                product.getProductName(),
                CategoryDTOMapper.shallowList(product.getCategories()),
                product.getProductPrice(),
                product.getProductDescription(),
                product.getProductStock(),
                product.isDiscontinued()
        );
    }

    public static Product toDomainShallow(ProductDTORequest request) {
        if (request == null) {
            return null;
        }
        return new Product(
                request.getProductId(),
                request.getProductName(),
                List.of(),
                request.getProductPrice(),
                request.getProductDescription(),
                request.getProductStock(),
                request.isDiscontinued()
        );
    }

    public static ProductDTOResponse toResponseShallow(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductDTOResponse(
                product.getProductId(),
                product.getProductName(),
                List.of(),
                product.getProductPrice(),
                product.getProductDescription(),
                product.getProductStock(),
                product.isDiscontinued()
        );
    }

    public static Product toDomainShallow(Product product) {
        if (product == null) {
            return null;
        }
        return new Product(
                product.getProductId(),
                product.getProductName(),
                List.of(),
                product.getProductPrice(),
                product.getProductDescription(),
                product.getProductStock(),
                product.isDiscontinued()
        );
    }

    public static List<Product> toDomainList(List<ProductDTORequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(ProductDTOMapper::toDomain)
                .toList();
    }

    public static List<Product> toDomainShallowList(List<ProductDTORequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(ProductDTOMapper::toDomainShallow)
                .toList();
    }

    public static List<ProductDTOResponse> toResponseList(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map(ProductDTOMapper::toResponse)
                .toList();
    }

    public static List<ProductDTOResponse> toResponseShallowList(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map(ProductDTOMapper::toResponseShallow)
                .toList();
    }

    public static List<Product> shallowList(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map(ProductDTOMapper::toDomainShallow)
                .toList();
    }
}
