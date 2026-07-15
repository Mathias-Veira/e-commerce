package com.project.e_commerce.domain.ports.out;

import com.project.e_commerce.domain.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category addCategory(Category category);
    List<Category> getAllCategories();
    List<Category> getCategoriesById(List<Integer> categoriesId);
    Optional<Category> getCategoryByName(String categoryName);
    void removeCategory(int categoryId);
}
