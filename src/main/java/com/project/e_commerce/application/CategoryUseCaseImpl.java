package com.project.e_commerce.application;

import com.project.e_commerce.application.exceptions.category.CategoryAlreadyExistsException;
import com.project.e_commerce.application.exceptions.category.CategoryNotFoundException;
import com.project.e_commerce.domain.Category;
import com.project.e_commerce.domain.exceptions.CategoryNotValidException;
import com.project.e_commerce.domain.ports.in.CategoryUseCase;
import com.project.e_commerce.domain.ports.out.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryUseCaseImpl implements CategoryUseCase {
    private final CategoryRepository categoryRepository;

    public CategoryUseCaseImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category addCategory(Category category) {
        if(!Category.validateName(category.getCategoryName())|| !Category.validateDescription(category.getCategoryDescription())|| !Category.validateProducts(category.getProducts())) throw new CategoryNotValidException("This category is not valid");
        Optional<Category> categoryOptional = categoryRepository.getCategoryByName(category.getCategoryName());
        if (categoryOptional.isPresent()) throw new CategoryAlreadyExistsException("Category already exists");
        return categoryRepository.addCategory(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.getAllCategories();
    }

    @Override
    public List<Category> getCategoriesById(List<Integer> categoriesId) {
        List<Category> categories = categoryRepository.getCategoriesById(categoriesId);
        if (categories.isEmpty()) throw new CategoryNotFoundException("Category could not be found");
        return categories;
    }

    @Override
    public Category updateCategory(Category category) {
        List<Category> categories = categoryRepository.getCategoriesById(new ArrayList<>(Arrays.asList(category.getCategoryId())));
        if(categories.isEmpty()) throw new CategoryNotFoundException("Category could not be found");
        if(!Category.validateName(category.getCategoryName())|| !Category.validateDescription(category.getCategoryDescription())|| !Category.validateProducts(category.getProducts())) throw new CategoryNotValidException("This category is not valid");
        return categoryRepository.addCategory(new Category(categories.get(0).getCategoryId(), category.getCategoryName(), category.getCategoryDescription(),category.getProducts()));
    }

    @Override
    public void removeCategory(int categoryId) {
        List<Category> categories = categoryRepository.getCategoriesById(new ArrayList<>(Arrays.asList(categoryId)));
        if (categories.isEmpty()) throw new CategoryNotFoundException("Category could not be found");
        categoryRepository.removeCategory(categoryId);
    }
}
