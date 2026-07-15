package com.project.e_commerce.infrastructure.web.controller;

import com.project.e_commerce.domain.Category;
import com.project.e_commerce.domain.ports.in.CategoryUseCase;
import com.project.e_commerce.infrastructure.web.dto.request.CategoryDTORequest;
import com.project.e_commerce.infrastructure.web.dto.response.CategoryDTOResponse;
import com.project.e_commerce.infrastructure.web.mapper.CategoryDTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryUseCase categoryUseCase;

    public CategoryController(CategoryUseCase categoryUseCase) {
        this.categoryUseCase = categoryUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<CategoryDTOResponse> addCategory(@RequestBody CategoryDTORequest request) {
        Category category = categoryUseCase.addCategory(CategoryDTOMapper.toDomain(request));
        return ResponseEntity.ok(CategoryDTOMapper.toResponse(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTOResponse>> getAllCategories() {
        List<Category> categories = categoryUseCase.getAllCategories();
        return ResponseEntity.ok(categories.stream().map(CategoryDTOMapper::toResponse).toList());
    }

    @GetMapping("/category")
    public ResponseEntity<List<CategoryDTOResponse>> getCategoriesById(@RequestParam List<Integer> categoriesId) {
        return ResponseEntity.ok(CategoryDTOMapper.toResponseList(categoryUseCase.getCategoriesById(categoriesId)));
    }

    @PutMapping("/update")
    public ResponseEntity<CategoryDTOResponse> updateCategory(@RequestBody CategoryDTORequest request) {
        Category category = categoryUseCase.updateCategory(CategoryDTOMapper.toDomain(request));
        return ResponseEntity.ok(CategoryDTOMapper.toResponse(category));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> removeCategory(@PathVariable int categoryId) {
        categoryUseCase.removeCategory(categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
