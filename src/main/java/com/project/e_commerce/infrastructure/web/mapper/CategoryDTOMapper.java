package com.project.e_commerce.infrastructure.web.mapper;

import com.project.e_commerce.domain.Category;
import com.project.e_commerce.infrastructure.web.dto.request.CategoryDTORequest;
import com.project.e_commerce.infrastructure.web.dto.response.CategoryDTOResponse;

import java.util.List;

public class CategoryDTOMapper {

    public static Category toDomain(CategoryDTORequest request) {
        if (request == null) {
            return null;
        }
        return new Category(
                request.getCategoryId(),
                request.getCategoryName(),
                request.getCategoryDescription(),
                ProductDTOMapper.shallowList(request.getProducts())
        );
    }

    public static CategoryDTOResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDTOResponse(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCategoryDescription(),
                ProductDTOMapper.shallowList(category.getProducts())
        );
    }

    public static Category toDomainShallow(CategoryDTORequest request) {
        if (request == null) {
            return null;
        }
        return new Category(
                request.getCategoryId(),
                request.getCategoryName(),
                request.getCategoryDescription(),
                List.of()
        );
    }

    public static Category toDomainShallow(Category category) {
        if (category == null) {
            return null;
        }
        return new Category(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCategoryDescription(),
                List.of()
        );
    }

    public static CategoryDTOResponse toResponseShallow(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDTOResponse(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCategoryDescription(),
                List.of()
        );
    }

    public static List<Category> toDomainList(List<CategoryDTORequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(CategoryDTOMapper::toDomain)
                .toList();
    }

    public static List<Category> toDomainShallowList(List<CategoryDTORequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(CategoryDTOMapper::toDomainShallow)
                .toList();
    }

    public static List<CategoryDTOResponse> toResponseList(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .map(CategoryDTOMapper::toResponse)
                .toList();
    }

    public static List<CategoryDTOResponse> toResponseShallowList(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .map(CategoryDTOMapper::toResponseShallow)
                .toList();
    }

    public static List<Category> shallowList(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .map(CategoryDTOMapper::toDomainShallow)
                .toList();
    }
}
