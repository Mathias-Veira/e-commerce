package com.project.e_commerce.domain.ports.in;

import com.project.e_commerce.domain.Category;

import java.util.List;

public interface CategoryUseCase {
    Category addCategory(Category category);
    List<Category> getAllCategories();
    List<Category> getCategoriesById(List<Integer> categoriesId);
    Category updateCategory(Category category);
    void removeCategory(int categoryId);
}
