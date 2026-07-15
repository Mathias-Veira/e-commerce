package com.project.e_commerce.infrastructure.persistence.category;

import com.project.e_commerce.domain.Category;
import com.project.e_commerce.domain.ports.out.CategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public class CategoryRepositoryJPA implements CategoryRepository {
    private final CategoryJPARepository categoryJPARepository;

    public CategoryRepositoryJPA(CategoryJPARepository categoryJPARepository) {
        this.categoryJPARepository = categoryJPARepository;
    }

    @Override
    public Category addCategory(Category category) {
        return CategoryMapper.toDomain(categoryJPARepository.save(CategoryMapper.toEntity(category)));
    }

    @Override
    public List<Category> getAllCategories() {
        return CategoryMapper.toDomainList(categoryJPARepository.findAll());
    }

    @Override
    public List<Category> getCategoriesById(List<Integer> categoriesId) {
        return CategoryMapper.toDomainList(categoryJPARepository.findAllById(categoriesId));
    }

    @Override
    public Optional<Category> getCategoryByName(String categoryName) {
        Optional<CategoryEntity> categoryEntityOptional = categoryJPARepository.findCategoryByName(categoryName);
        return categoryEntityOptional.map(CategoryMapper::toDomain);
    }

    @Override
    public void removeCategory(int categoryId) {
        categoryJPARepository.deleteById(categoryId);
    }
}
