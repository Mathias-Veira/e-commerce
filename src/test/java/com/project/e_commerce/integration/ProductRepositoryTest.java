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
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
class ProductRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

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

    // ── addProduct ────────────────────────────────────────────────────────────

    @Test
    void addProduct_ShouldPersistProductAndReturnWithGeneratedId() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Product saved = productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));

        assertThat(saved.getProductId()).isPositive();
        assertThat(saved.getProductName()).isEqualTo("Laptop");
        assertThat(saved.getProductPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(saved.getProductDescription()).isEqualTo("Gaming laptop");
        assertThat(saved.getProductStock()).isEqualTo(10);
        assertThat(saved.getCategories()).hasSize(1);
        assertThat(saved.getCategories().get(0).getCategoryName()).isEqualTo("Electronics");
        assertThat(saved.getCategories().get(0).getProducts()).isEmpty();
    }

    @Test
    void addProduct_ShouldBeRetrievableAfterPersisting() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Product saved = productRepository.addProduct(buildProduct("Phone", new BigDecimal("499.99"), "Smartphone", 20, List.of(category)));

        assertThat(productRepository.getProductById(saved.getProductId())).isPresent();
    }

    @Test
    void addProduct_EachProductShouldReceiveUniqueId() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Product first = productRepository.addProduct(buildProduct("Product A", new BigDecimal("10.00"), "Desc A", 5, List.of(category)));
        Product second = productRepository.addProduct(buildProduct("Product B", new BigDecimal("20.00"), "Desc B", 3, List.of(category)));

        assertThat(first.getProductId()).isNotEqualTo(second.getProductId());
    }

    @Test
    void addProduct_ShouldPersistAllFieldsWithoutModification() {
        String name = "Keyboard";
        BigDecimal price = new BigDecimal("79.99");
        String description = "Mechanical keyboard";
        int stock = 15;
        Category category = categoryRepository.addCategory(buildCategory("Peripherals", "Computer peripherals"));

        Product saved = productRepository.addProduct(buildProduct(name, price, description, stock, List.of(category)));
        Product retrieved = productRepository.getProductById(saved.getProductId()).orElseThrow();

        assertThat(retrieved.getProductName()).isEqualTo(name);
        assertThat(retrieved.getProductPrice()).isEqualByComparingTo(price);
        assertThat(retrieved.getProductDescription()).isEqualTo(description);
        assertThat(retrieved.getProductStock()).isEqualTo(stock);
        assertThat(retrieved.getCategories()).hasSize(1);
        assertThat(retrieved.getCategories().get(0).getCategoryName()).isEqualTo("Peripherals");
        assertThat(retrieved.getCategories().get(0).getProducts()).isEmpty();
    }

    @Test
    void addProduct_WithMultipleCategories_ShouldPersistAllCategories() {
        Category electronics = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Category gaming = categoryRepository.addCategory(buildCategory("Gaming", "Gaming products"));
        Product saved = productRepository.addProduct(buildProduct("Gaming Laptop", new BigDecimal("1499.99"), "High-end gaming laptop", 5, List.of(electronics, gaming)));
        Product retrieved = productRepository.getProductById(saved.getProductId()).orElseThrow();

        assertThat(retrieved.getCategories()).hasSize(2);
        assertThat(retrieved.getCategories()).allSatisfy(cat -> assertThat(cat.getProducts()).isEmpty());
    }

    // ── getAllProducts ─────────────────────────────────────────────────────────

    @Test
    void getAllProducts_WithEmptyDatabase_ShouldReturnEmptyPage() {
        Page<Product> result = productRepository.getAllProducts(PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Product A", new BigDecimal("10.00"), "Desc A", 1, List.of(category)));
        productRepository.addProduct(buildProduct("Product B", new BigDecimal("20.00"), "Desc B", 2, List.of(category)));
        productRepository.addProduct(buildProduct("Product C", new BigDecimal("30.00"), "Desc C", 3, List.of(category)));

        Page<Product> result = productRepository.getAllProducts(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void getAllProducts_ShouldRespectPageSize() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Product A", new BigDecimal("10.00"), "Desc A", 1, List.of(category)));
        productRepository.addProduct(buildProduct("Product B", new BigDecimal("20.00"), "Desc B", 2, List.of(category)));
        productRepository.addProduct(buildProduct("Product C", new BigDecimal("30.00"), "Desc C", 3, List.of(category)));

        Page<Product> result = productRepository.getAllProducts(PageRequest.of(0, 2));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void getAllProducts_ShouldReturnCorrectPage() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Product A", new BigDecimal("10.00"), "Desc A", 1, List.of(category)));
        productRepository.addProduct(buildProduct("Product B", new BigDecimal("20.00"), "Desc B", 2, List.of(category)));
        productRepository.addProduct(buildProduct("Product C", new BigDecimal("30.00"), "Desc C", 3, List.of(category)));

        Page<Product> firstPage = productRepository.getAllProducts(PageRequest.of(0, 2));
        Page<Product> secondPage = productRepository.getAllProducts(PageRequest.of(1, 2));

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(secondPage.isLast()).isTrue();
    }

    @Test
    void getAllProducts_PageBeyondTotal_ShouldReturnEmptyPage() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Product A", new BigDecimal("10.00"), "Desc A", 1, List.of(category)));

        Page<Product> result = productRepository.getAllProducts(PageRequest.of(5, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllProducts_ShouldReturnProductsWithCategoriesHavingEmptyProductsList() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));

        Page<Product> result = productRepository.getAllProducts(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategories()).hasSize(1);
        assertThat(result.getContent().get(0).getCategories().get(0).getProducts()).isEmpty();
    }

    // ── getProductById ────────────────────────────────────────────────────────

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Product saved = productRepository.addProduct(buildProduct("Monitor", new BigDecimal("299.99"), "4K monitor", 8, List.of(category)));

        Optional<Product> result = productRepository.getProductById(saved.getProductId());

        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(saved.getProductId());
        assertThat(result.get().getProductName()).isEqualTo("Monitor");
        assertThat(result.get().getProductPrice()).isEqualByComparingTo(new BigDecimal("299.99"));
        assertThat(result.get().getCategories()).hasSize(1);
        assertThat(result.get().getCategories().get(0).getProducts()).isEmpty();
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldReturnEmpty() {
        Optional<Product> result = productRepository.getProductById(999999);

        assertThat(result).isEmpty();
    }

    @Test
    void getProductById_WithEmptyDatabase_ShouldReturnEmpty() {
        Optional<Product> result = productRepository.getProductById(1);

        assertThat(result).isEmpty();
    }

    @Test
    void getProductById_ShouldReturnCorrectProductAmongMultiple() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Product product1 = productRepository.addProduct(buildProduct("Product A", new BigDecimal("10.00"), "Desc A", 1, List.of(category)));
        Product product2 = productRepository.addProduct(buildProduct("Product B", new BigDecimal("20.00"), "Desc B", 2, List.of(category)));

        Optional<Product> result = productRepository.getProductById(product1.getProductId());

        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(product1.getProductId());
        assertThat(result.get().getProductName()).isEqualTo("Product A");
        assertThat(result.get().getProductName()).isNotEqualTo(product2.getProductName());
    }

    // ── productAlreadyExists ──────────────────────────────────────────────────

    @Test
    void productAlreadyExists_WhenExactMatchExists_ShouldReturnTrue() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));

        boolean result = productRepository.productAlreadyExists("Laptop", new BigDecimal("999.99"), "Gaming laptop");

        assertThat(result).isTrue();
    }

    @Test
    void productAlreadyExists_WithDifferentName_ShouldReturnFalse() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));

        boolean result = productRepository.productAlreadyExists("Desktop", new BigDecimal("999.99"), "Gaming laptop");

        assertThat(result).isFalse();
    }

    @Test
    void productAlreadyExists_WithDifferentPrice_ShouldReturnFalse() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));

        boolean result = productRepository.productAlreadyExists("Laptop", new BigDecimal("799.99"), "Gaming laptop");

        assertThat(result).isFalse();
    }

    @Test
    void productAlreadyExists_WithDifferentDescription_ShouldReturnFalse() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));

        boolean result = productRepository.productAlreadyExists("Laptop", new BigDecimal("999.99"), "Office laptop");

        assertThat(result).isFalse();
    }

    @Test
    void productAlreadyExists_WithEmptyDatabase_ShouldReturnFalse() {
        boolean result = productRepository.productAlreadyExists("Laptop", new BigDecimal("999.99"), "Gaming laptop");

        assertThat(result).isFalse();
    }

    @Test
    void productAlreadyExists_WhenOnlyTwoFieldsMatch_ShouldReturnFalse() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));

        boolean nameAndPriceOnly = productRepository.productAlreadyExists("Laptop", new BigDecimal("999.99"), "Different description");
        boolean nameAndDescOnly = productRepository.productAlreadyExists("Laptop", new BigDecimal("1.00"), "Gaming laptop");
        boolean priceAndDescOnly = productRepository.productAlreadyExists("Different", new BigDecimal("999.99"), "Gaming laptop");

        assertThat(nameAndPriceOnly).isFalse();
        assertThat(nameAndDescOnly).isFalse();
        assertThat(priceAndDescOnly).isFalse();
    }

    // ── removeStock ───────────────────────────────────────────────────────────

    @Test
    void removeStock_ShouldDecrementStockByGivenQuantity() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Product saved = productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 10, List.of(category)));

        productRepository.removeStock(saved.getProductId(), 3);

        Product updated = productRepository.getProductById(saved.getProductId()).orElseThrow();
        assertThat(updated.getProductStock()).isEqualTo(7);
    }

    @Test
    void removeStock_WithFullQuantity_ShouldLeaveStockAtZero() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Product saved = productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 5, List.of(category)));

        productRepository.removeStock(saved.getProductId(), 5);

        Product updated = productRepository.getProductById(saved.getProductId()).orElseThrow();
        assertThat(updated.getProductStock()).isEqualTo(0);
    }

    @Test
    void removeStock_WithQuantityOfOne_ShouldDecrementByOne() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Product saved = productRepository.addProduct(buildProduct("Mouse", new BigDecimal("29.99"), "Wireless mouse", 3, List.of(category)));

        productRepository.removeStock(saved.getProductId(), 1);

        Product updated = productRepository.getProductById(saved.getProductId()).orElseThrow();
        assertThat(updated.getProductStock()).isEqualTo(2);
    }

    // ── addStock ──────────────────────────────────────────────────────────────

    @Test
    void addStock_ShouldIncrementStockByGivenQuantity() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Product saved = productRepository.addProduct(buildProduct("Laptop", new BigDecimal("999.99"), "Gaming laptop", 5, List.of(category)));

        productRepository.addStock(saved.getProductId(), 3);

        Product updated = productRepository.getProductById(saved.getProductId()).orElseThrow();
        assertThat(updated.getProductStock()).isEqualTo(8);
    }

    @Test
    void addStock_FromZero_ShouldRestoreStock() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Product saved = productRepository.addProduct(buildProduct("Keyboard", new BigDecimal("79.99"), "Mechanical keyboard", 0, List.of(category)));

        productRepository.addStock(saved.getProductId(), 5);

        Product updated = productRepository.getProductById(saved.getProductId()).orElseThrow();
        assertThat(updated.getProductStock()).isEqualTo(5);
    }

    @Test
    void removeAndAddStock_ShouldRestoreOriginalStock() {
        Category category = categoryRepository.addCategory(buildCategory("Electronics", "Electronic devices"));
        Product saved = productRepository.addProduct(buildProduct("Monitor", new BigDecimal("299.99"), "4K monitor", 10, List.of(category)));

        productRepository.removeStock(saved.getProductId(), 4);
        productRepository.addStock(saved.getProductId(), 4);

        Product updated = productRepository.getProductById(saved.getProductId()).orElseThrow();
        assertThat(updated.getProductStock()).isEqualTo(10);
    }
}
