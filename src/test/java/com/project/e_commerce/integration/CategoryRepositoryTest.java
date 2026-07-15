package com.project.e_commerce.integration;

import com.project.e_commerce.domain.Category;
import com.project.e_commerce.domain.Product;
import com.project.e_commerce.domain.ports.out.CategoryRepository;
import com.project.e_commerce.domain.ports.out.ProductRepository;
import com.project.e_commerce.infrastructure.persistence.category.CategoryRepositoryJPA;
import com.project.e_commerce.infrastructure.persistence.product.ProductRepositoryJPA;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({ProductRepositoryJPA.class, CategoryRepositoryJPA.class})
class CategoryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    private Category buildCategory(String name, String description) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setCategoryDescription(description);
        category.setProducts(new ArrayList<>());
        return category;
    }

    private Product buildProduct(String name, BigDecimal price, String description, int stock, List<Category> categories) {
        Product product = new Product();
        product.setProductName(name);
        product.setProductPrice(price);
        product.setProductDescription(description);
        product.setProductStock(stock);
        product.setCategories(categories);
        return product;
    }

    // ── addCategory ───────────────────────────────────────────────────────────

    @Test
    void addCategory_ShouldPersistCategoryAndReturnWithGeneratedId() {
        Category saved = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));

        assertThat(saved.getCategoryId()).isPositive();
        assertThat(saved.getCategoryName()).isEqualTo("Electronics");
        assertThat(saved.getCategoryDescription()).isEqualTo("Electronic devices");
    }

    @Test
    void addCategory_ShouldBeRetrievableAfterPersisting() {
        Category saved = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));

        assertThat(categoryRepository.getCategoryByName(saved.getCategoryName())).isPresent();
    }

    @Test
    void addCategory_EachCategoryShouldReceiveUniqueId() {
        Category first = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Category second = categoryRepository.addCategory(buildCategory("Gaming", "Gaming products"));

        assertThat(first.getCategoryId()).isNotEqualTo(second.getCategoryId());
    }

    @Test
    void addCategory_ShouldPersistAllFieldsWithoutModification() {
        String name = "Peripherals";
        String description = "Computer peripherals";

        categoryRepository.addCategory(buildCategory(name, description));
        Category retrieved = categoryRepository.getCategoryByName(name).orElseThrow();

        assertThat(retrieved.getCategoryName()).isEqualTo(name);
        assertThat(retrieved.getCategoryDescription()).isEqualTo(description);
    }

    // ── getAllCategories ───────────────────────────────────────────────────────

    @Test
    void getAllCategories_WithEmptyDatabase_ShouldReturnEmptyList() {
        List<Category> result = categoryRepository.getAllCategories();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        categoryRepository.addCategory(buildCategory("Gaming", "Gaming products"));
        categoryRepository.addCategory(buildCategory("Peripherals", "Computer peripherals"));

        List<Category> result = categoryRepository.getAllCategories();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Category::getCategoryName)
                .containsExactlyInAnyOrder("Electronics", "Gaming", "Peripherals");
    }

    @Test
    void getAllCategories_ShouldReturnCategoryWithAssociatedProducts() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));
        testEntityManager.flush();
        testEntityManager.getEntityManager().clear();

        List<Category> result = categoryRepository.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProducts()).hasSize(1);
        assertThat(result.get(0).getProducts().get(0).getProductName()).isEqualTo("Laptop");
        assertThat(result.get(0).getProducts().get(0).getCategories()).isEmpty();
    }

    @Test
    void getAllCategories_CategoryWithMultipleProducts_ShouldReturnAllAssociatedProducts() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));
        productRepository.addProduct(buildProduct("Phone", new BigDecimal("499.99"), "Smartphone", 20, List.of(category)));
        testEntityManager.flush();
        testEntityManager.getEntityManager().clear();

        List<Category> result = categoryRepository.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProducts()).hasSize(2);
        assertThat(result.get(0).getProducts()).allSatisfy(p -> assertThat(p.getCategories()).isEmpty());
    }

    // ── getCategoriesById ─────────────────────────────────────────────────────

    @Test
    void getCategoriesById_ShouldReturnMatchingCategories() {
        Category electronics = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Category gaming = categoryRepository.addCategory(buildCategory("Gaming", "Gaming products"));

        List<Category> result = categoryRepository.getCategoriesById(List.of(electronics.getCategoryId(), gaming.getCategoryId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getCategoryName)
                .containsExactlyInAnyOrder("Electronics", "Gaming");
    }

    @Test
    void getCategoriesById_WithNonExistentIds_ShouldReturnEmptyList() {
        List<Category> result = categoryRepository.getCategoriesById(List.of(999999, 888888));

        assertThat(result).isEmpty();
    }

    @Test
    void getCategoriesById_WithEmptyList_ShouldReturnEmptyList() {
        categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));

        List<Category> result = categoryRepository.getCategoriesById(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void getCategoriesById_WithMixedIds_ShouldReturnOnlyExistingCategories() {
        Category electronics = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));

        List<Category> result = categoryRepository.getCategoriesById(List.of(electronics.getCategoryId(), 999999));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Electronics");
    }

    @Test
    void getCategoriesById_WithSingleId_ShouldReturnSingleCategory() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));

        List<Category> result = categoryRepository.getCategoriesById(List.of(category.getCategoryId()));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo(category.getCategoryId());
        assertThat(result.get(0).getCategoryName()).isEqualTo("Electronics");
    }

    @Test
    void getCategoriesById_ShouldReturnCategoriesWithAssociatedProducts() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));
        int categoryId = category.getCategoryId();
        testEntityManager.flush();
        testEntityManager.getEntityManager().clear();

        List<Category> result = categoryRepository.getCategoriesById(List.of(categoryId));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProducts()).hasSize(1);
        assertThat(result.get(0).getProducts().get(0).getProductName()).isEqualTo("Laptop");
        assertThat(result.get(0).getProducts().get(0).getCategories()).isEmpty();
    }

    // ── getCategoryByName ─────────────────────────────────────────────────────

    @Test
    void getCategoryByName_WhenCategoryExists_ShouldReturnCategory() {
        categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));

        Optional<Category> result = categoryRepository.getCategoryByName("Electronics");

        assertThat(result).isPresent();
        assertThat(result.get().getCategoryName()).isEqualTo("Electronics");
        assertThat(result.get().getCategoryDescription()).isEqualTo("Electronic devices");
    }

    @Test
    void getCategoryByName_WhenCategoryDoesNotExist_ShouldReturnEmpty() {
        Optional<Category> result = categoryRepository.getCategoryByName("NonExistent");

        assertThat(result).isEmpty();
    }

    @Test
    void getCategoryByName_WithEmptyDatabase_ShouldReturnEmpty() {
        Optional<Category> result = categoryRepository.getCategoryByName("Electronics");

        assertThat(result).isEmpty();
    }

    @Test
    void getCategoryByName_ShouldReturnCorrectCategoryAmongMultiple() {
        categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        categoryRepository.addCategory(buildCategory("Gaming", "Gaming products"));

        Optional<Category> result = categoryRepository.getCategoryByName("Gaming");

        assertThat(result).isPresent();
        assertThat(result.get().getCategoryName()).isEqualTo("Gaming");
        assertThat(result.get().getCategoryDescription()).isEqualTo("Gaming products");
        assertThat(result.get().getCategoryName()).isNotEqualTo("Electronics");
    }

    @Test
    void getCategoryByName_ShouldReturnCategoryWithAssociatedProducts() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));
        testEntityManager.flush();
        testEntityManager.getEntityManager().clear();

        Optional<Category> result = categoryRepository.getCategoryByName("Electronics");

        assertThat(result).isPresent();
        assertThat(result.get().getProducts()).hasSize(1);
        assertThat(result.get().getProducts().get(0).getProductName()).isEqualTo("Laptop");
        assertThat(result.get().getProducts().get(0).getCategories()).isEmpty();
    }

    // ── removeCategory ────────────────────────────────────────────────────────

    @Test
    void removeCategory_ShouldDeleteCategory() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));

        categoryRepository.removeCategory(category.getCategoryId());

        assertThat(categoryRepository.getCategoryByName("Electronics")).isEmpty();
    }

    @Test
    void removeCategory_ShouldNotBeRetrievableById() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));

        categoryRepository.removeCategory(category.getCategoryId());

        assertThat(categoryRepository.getCategoriesById(List.of(category.getCategoryId()))).isEmpty();
    }

    @Test
    void removeCategory_ShouldNotAffectOtherCategories() {
        Category electronics = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        categoryRepository.addCategory(buildCategory("Gaming", "Gaming products"));

        categoryRepository.removeCategory(electronics.getCategoryId());

        List<Category> remaining = categoryRepository.getAllCategories();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getCategoryName()).isEqualTo("Gaming");
    }

    @Test
    void removeCategory_ShouldDecreaseCount() {
        categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Category gaming = categoryRepository.addCategory(buildCategory("Gaming", "Gaming products"));
        categoryRepository.addCategory(buildCategory("Peripherals", "Computer peripherals"));

        categoryRepository.removeCategory(gaming.getCategoryId());

        assertThat(categoryRepository.getAllCategories()).hasSize(2);
    }
}
