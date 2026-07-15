package com.project.e_commerce.unit.category;

import com.project.e_commerce.application.CategoryUseCaseImpl;
import com.project.e_commerce.application.exceptions.category.CategoryAlreadyExistsException;
import com.project.e_commerce.application.exceptions.category.CategoryNotFoundException;
import com.project.e_commerce.domain.Category;
import com.project.e_commerce.domain.exceptions.CategoryNotValidException;
import com.project.e_commerce.domain.ports.out.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CategoryUseCaseImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryUseCaseImpl categoryUseCaseImpl;

    @BeforeEach
    void setUp() {
        categoryUseCaseImpl = new CategoryUseCaseImpl(categoryRepository);
    }

    private Category validCategory() {
        return new Category(0, "Electronics", "All electronic devices and accessories.", List.of());
    }

    private Category persistedCategory(int id) {
        return new Category(id, "Electronics", "All electronic devices and accessories.", List.of());
    }

    // -------------------------------------------------------------------------
    // addCategory — validation, uniqueness, happy path
    // -------------------------------------------------------------------------

    @Test
    void addCategory_blankName_throwsCategoryNotValidException_andNeverPersists() {
        Category category = new Category(0, "   ", "Valid description", List.of());
        assertThrows(CategoryNotValidException.class, () -> categoryUseCaseImpl.addCategory(category));
        verify(categoryRepository, never()).addCategory(any());
    }

    @Test
    void addCategory_nameExceedsMaxLength_throwsCategoryNotValidException_andNeverPersists() {
        Category category = new Category(0, "x".repeat(101), "Valid description", List.of());
        assertThrows(CategoryNotValidException.class, () -> categoryUseCaseImpl.addCategory(category));
        verify(categoryRepository, never()).addCategory(any());
    }

    @Test
    void addCategory_descriptionExceedsMaxLength_throwsCategoryNotValidException_andNeverPersists() {
        Category category = new Category(0, "Electronics", "x".repeat(501), List.of());
        assertThrows(CategoryNotValidException.class, () -> categoryUseCaseImpl.addCategory(category));
        verify(categoryRepository, never()).addCategory(any());
    }

    @Test
    void addCategory_nameAlreadyExists_throwsCategoryAlreadyExistsException_andNeverPersists() {
        when(categoryRepository.getCategoryByName("Electronics")).thenReturn(Optional.of(persistedCategory(1)));
        Category category = new Category(0, "Electronics", "Another description", List.of());
        assertThrows(CategoryAlreadyExistsException.class, () -> categoryUseCaseImpl.addCategory(category));
        verify(categoryRepository, never()).addCategory(any());
    }

    @Test
    void addCategory_nameAlreadyExistsCaseInsensitive_throwsCategoryAlreadyExistsException_andNeverPersists() {
        when(categoryRepository.getCategoryByName("ELECTRONICS")).thenReturn(Optional.of(persistedCategory(1)));
        Category category = new Category(0, "ELECTRONICS", "Another description", List.of());
        assertThrows(CategoryAlreadyExistsException.class, () -> categoryUseCaseImpl.addCategory(category));
        verify(categoryRepository, never()).addCategory(any());
    }

    @Test
    void addCategory_emptyDescription_isValid_doesNotThrow() {
        when(categoryRepository.getCategoryByName("Electronics")).thenReturn(Optional.empty());
        Category category = new Category(0, "Electronics", "", List.of());
        when(categoryRepository.addCategory(any())).thenAnswer(inv -> inv.getArgument(0));
        assertDoesNotThrow(() -> categoryUseCaseImpl.addCategory(category));
    }

    @Test
    void addCategory_validCategory_callsRepositoryOnceAndReturnsPersistedCategory() {
        when(categoryRepository.getCategoryByName("Electronics")).thenReturn(Optional.empty());
        Category persisted = persistedCategory(1);
        when(categoryRepository.addCategory(any())).thenReturn(persisted);
        Category result = categoryUseCaseImpl.addCategory(validCategory());
        verify(categoryRepository, times(1)).addCategory(any());
        assertEquals(1, result.getCategoryId());
    }

    @Test
    void addCategory_validCategory_returnsExactlyWhatRepositoryReturns() {
        when(categoryRepository.getCategoryByName("Electronics")).thenReturn(Optional.empty());
        Category persisted = persistedCategory(42);
        when(categoryRepository.addCategory(any())).thenReturn(persisted);
        Category result = categoryUseCaseImpl.addCategory(validCategory());
        assertEquals(persisted, result);
    }

    // -------------------------------------------------------------------------
    // getAllCategories
    // -------------------------------------------------------------------------

    @Test
    void getAllCategories_returnsListFromRepository() {
        List<Category> categories = List.of(
                new Category(1, "Electronics", "desc", List.of()),
                new Category(2, "Clothing", "desc", List.of())
        );
        when(categoryRepository.getAllCategories()).thenReturn(categories);
        List<Category> result = categoryUseCaseImpl.getAllCategories();
        assertEquals(2, result.size());
        verify(categoryRepository, times(1)).getAllCategories();
    }

    @Test
    void getAllCategories_emptyRepository_returnsEmptyList() {
        when(categoryRepository.getAllCategories()).thenReturn(Collections.emptyList());
        List<Category> result = categoryUseCaseImpl.getAllCategories();
        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // getCategoriesById
    // -------------------------------------------------------------------------

    @Test
    void getCategoriesById_existingId_returnsCategory() {
        Category category = persistedCategory(1);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(category));
        List<Category> result = categoryUseCaseImpl.getCategoriesById(List.of(1));
        assertEquals(List.of(category), result);
    }

    @Test
    void getCategoriesById_nonExistingId_throwsCategoryNotFoundException() {
        when(categoryRepository.getCategoriesById(List.of(99))).thenReturn(Collections.emptyList());
        assertThrows(CategoryNotFoundException.class, () -> categoryUseCaseImpl.getCategoriesById(List.of(99)));
    }

    // -------------------------------------------------------------------------
    // updateCategory — not found, validation, happy path
    // -------------------------------------------------------------------------

    @Test
    void updateCategory_categoryDoesNotExist_throwsCategoryNotFoundException_andNeverPersists() {
        Category update = new Category(99, "Electronics", "desc", List.of());
        when(categoryRepository.getCategoriesById(List.of(99))).thenReturn(Collections.emptyList());
        assertThrows(CategoryNotFoundException.class, () -> categoryUseCaseImpl.updateCategory(update));
        verify(categoryRepository, never()).addCategory(any());
    }

    @Test
    void updateCategory_blankName_throwsCategoryNotValidException_andNeverPersists() {
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(persistedCategory(1)));
        Category update = new Category(1, "   ", "desc", List.of());
        assertThrows(CategoryNotValidException.class, () -> categoryUseCaseImpl.updateCategory(update));
        verify(categoryRepository, never()).addCategory(any());
    }

    @Test
    void updateCategory_nameExceedsMaxLength_throwsCategoryNotValidException_andNeverPersists() {
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(persistedCategory(1)));
        Category update = new Category(1, "x".repeat(101), "desc", List.of());
        assertThrows(CategoryNotValidException.class, () -> categoryUseCaseImpl.updateCategory(update));
        verify(categoryRepository, never()).addCategory(any());
    }

    @Test
    void updateCategory_descriptionExceedsMaxLength_throwsCategoryNotValidException_andNeverPersists() {
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(persistedCategory(1)));
        Category update = new Category(1, "Electronics", "x".repeat(501), List.of());
        assertThrows(CategoryNotValidException.class, () -> categoryUseCaseImpl.updateCategory(update));
        verify(categoryRepository, never()).addCategory(any());
    }

    @Test
    void updateCategory_validCategory_returnsUpdatedCategory() {
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(persistedCategory(1)));
        Category update = new Category(1, "Updated Name", "Updated description", List.of());
        when(categoryRepository.addCategory(any())).thenReturn(update);
        Category result = categoryUseCaseImpl.updateCategory(update);
        assertEquals("Updated Name", result.getCategoryName());
        assertEquals("Updated description", result.getCategoryDescription());
    }

    @Test
    void updateCategory_preservesCategoryId() {
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(persistedCategory(1)));
        when(categoryRepository.addCategory(any())).thenAnswer(inv -> inv.getArgument(0));
        Category update = new Category(1, "Updated Name", "desc", List.of());
        Category result = categoryUseCaseImpl.updateCategory(update);
        assertEquals(1, result.getCategoryId());
    }

    // -------------------------------------------------------------------------
    // removeCategory — not found, happy path
    // -------------------------------------------------------------------------

    @Test
    void removeCategory_nonExistingId_throwsCategoryNotFoundException_andNeverRemoves() {
        when(categoryRepository.getCategoriesById(List.of(99))).thenReturn(Collections.emptyList());
        assertThrows(CategoryNotFoundException.class, () -> categoryUseCaseImpl.removeCategory(99));
        verify(categoryRepository, never()).removeCategory(anyInt());
    }

    @Test
    void removeCategory_existingId_callsRepositoryRemoveOnce() {
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(persistedCategory(1)));
        doNothing().when(categoryRepository).removeCategory(1);
        categoryUseCaseImpl.removeCategory(1);
        verify(categoryRepository, times(1)).removeCategory(1);
    }
}
