package com.project.e_commerce.unit.product;

import com.project.e_commerce.application.ProductUseCaseImpl;
import com.project.e_commerce.application.exceptions.product.ProductAlreadyExistsException;
import com.project.e_commerce.application.exceptions.product.ProductNotFoundException;
import com.project.e_commerce.domain.Category;
import com.project.e_commerce.domain.Product;
import com.project.e_commerce.domain.exceptions.ProductNotValidException;
import com.project.e_commerce.domain.ports.out.CategoryRepository;
import com.project.e_commerce.domain.ports.out.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductUseCaseImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ProductUseCaseImpl productUseCaseImpl;

    @BeforeEach
    void setUp() {
        productUseCaseImpl = new ProductUseCaseImpl(productRepository, categoryRepository);
    }

    private Category sampleCategory() {
        return new Category(1, "Electronics", "Electronic products", List.of());
    }

    private Product validProduct() {
        return new Product(0, "Test Product", List.of(sampleCategory()), new BigDecimal("9.99"), "A valid description", 10, false);
    }

    private Product persistedProduct(int id) {
        return new Product(id, "Test Product", List.of(sampleCategory()), new BigDecimal("9.99"), "A valid description", 10, false);
    }

    // --- addProduct ---

    @Test
    void addProduct_negativePrice_throwsProductNotValidException_andNeverPersists() {
        Product product = new Product(0, "Test", List.of(sampleCategory()), new BigDecimal("-0.01"), "desc", 10, false);
        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.addProduct(product));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void addProduct_priceWithMoreThan2Decimals_throwsProductNotValidException_andNeverPersists() {
        Product product = new Product(0, "Test", List.of(sampleCategory()), new BigDecimal("9.999"), "desc", 10, false);
        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.addProduct(product));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void addProduct_zeroPrice_isValid_doesNotThrow() {
        Product product = new Product(0, "Test", List.of(sampleCategory()), BigDecimal.ZERO, "desc", 10, false);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(sampleCategory()));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));
        assertDoesNotThrow(() -> productUseCaseImpl.addProduct(product));
    }

    @Test
    void addProduct_negativeStock_throwsProductNotValidException_andNeverPersists() {
        Product product = new Product(0, "Test", List.of(sampleCategory()), new BigDecimal("9.99"), "desc", -1, false);
        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.addProduct(product));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void addProduct_zeroStock_isValid_doesNotThrow() {
        Product product = new Product(0, "Test", List.of(sampleCategory()), new BigDecimal("9.99"), "desc", 0, false);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(sampleCategory()));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));
        assertDoesNotThrow(() -> productUseCaseImpl.addProduct(product));
        assertTrue(product.isOutOfStock());
    }

    @Test
    void addProduct_descriptionExceeds2000Chars_throwsProductNotValidException_andNeverPersists() {
        Product product = new Product(0, "Test", List.of(sampleCategory()), new BigDecimal("9.99"), "x".repeat(2001), 10, false);
        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.addProduct(product));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void addProduct_descriptionExactly2000Chars_isValid_doesNotThrow() {
        Product product = new Product(0, "Test", List.of(sampleCategory()), new BigDecimal("9.99"), "x".repeat(2000), 10, false);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(sampleCategory()));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));
        assertDoesNotThrow(() -> productUseCaseImpl.addProduct(product));
    }

    @Test
    void addProduct_emptyDescription_isValid_doesNotThrow() {
        Product product = new Product(0, "Test", List.of(sampleCategory()), new BigDecimal("9.99"), "", 10, false);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(sampleCategory()));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));
        assertDoesNotThrow(() -> productUseCaseImpl.addProduct(product));
    }

    @Test
    void addProduct_emptyCategories_throwsProductNotValidException_andNeverPersists() {
        Product product = new Product(0, "Test", List.of(), new BigDecimal("9.99"), "desc", 10, false);
        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.addProduct(product));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void addProduct_validProduct_callsRepositoryOnceAndReturnsPersistedProduct() {
        Product persisted = persistedProduct(1);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(sampleCategory()));
        when(productRepository.addProduct(any())).thenReturn(persisted);

        Product result = productUseCaseImpl.addProduct(validProduct());

        verify(productRepository, times(1)).addProduct(any());
        assertEquals(1, result.getProductId());
    }

    @Test
    void addProduct_validProduct_returnsExactlyWhatRepositoryReturns() {
        Product persisted = persistedProduct(42);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(sampleCategory()));
        when(productRepository.addProduct(any())).thenReturn(persisted);

        Product result = productUseCaseImpl.addProduct(validProduct());

        assertEquals(persisted, result);
    }

    @Test
    void addProduct_duplicateNamePriceAndDescription_throwsProductAlreadyExistsException_andNeverPersists() {
        when(productRepository.productAlreadyExists(anyString(), any(BigDecimal.class), anyString())).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productUseCaseImpl.addProduct(validProduct()));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void addProduct_duplicateCheck_calledWithProductNamePriceAndDescription() {
        Product product = validProduct();
        when(productRepository.productAlreadyExists(anyString(), any(BigDecimal.class), anyString())).thenReturn(false);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(sampleCategory()));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));

        productUseCaseImpl.addProduct(product);

        verify(productRepository, times(1)).productAlreadyExists(
                eq(product.getProductName()), eq(product.getProductPrice()), eq(product.getProductDescription()));
    }

    @Test
    void addProduct_notDuplicate_persistsSuccessfully() {
        when(productRepository.productAlreadyExists(anyString(), any(BigDecimal.class), anyString())).thenReturn(false);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(sampleCategory()));
        Product persisted = persistedProduct(1);
        when(productRepository.addProduct(any())).thenReturn(persisted);

        Product result = productUseCaseImpl.addProduct(validProduct());

        assertEquals(persisted, result);
        verify(productRepository, times(1)).addProduct(any());
    }

    @Test
    void addProduct_invalidProduct_neverChecksForDuplicates() {
        // Validation is checked before the duplicate check: an invalid product must throw
        // ProductNotValidException without ever calling productAlreadyExists
        Product invalidProduct = new Product(0, "Test Product", List.of(sampleCategory()), new BigDecimal("-1"), "desc", 10, false);
        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.addProduct(invalidProduct));
        verify(productRepository, never()).productAlreadyExists(any(), any(), any());
        verify(productRepository, never()).addProduct(any());
    }

    // --- addProduct: referenced categories must exist ---

    @Test
    void addProduct_allReferencedCategoriesExist_doesNotThrow() {
        Product product = validProduct();
        when(productRepository.productAlreadyExists(anyString(), any(BigDecimal.class), anyString())).thenReturn(false);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(sampleCategory()));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> productUseCaseImpl.addProduct(product));
        verify(productRepository, times(1)).addProduct(any());
    }

    @Test
    void addProduct_someReferencedCategoriesDoNotExist_throwsProductNotValidException_andNeverPersists() {
        Category existingCategory = sampleCategory();
        Category missingCategory = new Category(2, "Books", "Books and literature", List.of());
        Product product = new Product(0, "Test Product", List.of(existingCategory, missingCategory), new BigDecimal("9.99"), "desc", 10, false);
        when(productRepository.productAlreadyExists(anyString(), any(BigDecimal.class), anyString())).thenReturn(false);
        when(categoryRepository.getCategoriesById(List.of(1, 2))).thenReturn(List.of(existingCategory));

        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.addProduct(product));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void addProduct_noReferencedCategoriesExist_throwsProductNotValidException_andNeverPersists() {
        Product product = validProduct();
        when(productRepository.productAlreadyExists(anyString(), any(BigDecimal.class), anyString())).thenReturn(false);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of());

        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.addProduct(product));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void addProduct_categoryExistenceCheck_calledWithCategoryIdsFromProduct() {
        Product product = validProduct();
        when(productRepository.productAlreadyExists(anyString(), any(BigDecimal.class), anyString())).thenReturn(false);
        when(categoryRepository.getCategoriesById(List.of(1))).thenReturn(List.of(sampleCategory()));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));

        productUseCaseImpl.addProduct(product);

        verify(categoryRepository, times(1)).getCategoriesById(List.of(1));
    }

    // --- getAllProducts ---

    @Test
    void getAllProducts_returnsPageFromRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> products = new PageImpl<>(List.of(persistedProduct(1), persistedProduct(2)), pageable, 2);
        when(productRepository.getAllProducts(pageable)).thenReturn(products);

        Page<Product> result = productUseCaseImpl.getAllProducts(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(productRepository, times(1)).getAllProducts(pageable);
    }

    @Test
    void getAllProducts_emptyRepository_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.getAllProducts(pageable)).thenReturn(Page.empty(pageable));

        Page<Product> result = productUseCaseImpl.getAllProducts(pageable);

        assertTrue(result.isEmpty());
    }

    // --- getProductById ---

    @Test
    void getProductById_existingId_returnsProduct() {
        Product product = persistedProduct(1);
        when(productRepository.getProductById(1)).thenReturn(Optional.of(product));

        Product result = productUseCaseImpl.getProductById(1);

        assertEquals(product, result);
    }

    @Test
    void getProductById_nonExistingId_throwsProductNotFoundException() {
        when(productRepository.getProductById(99)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productUseCaseImpl.getProductById(99));
    }

    @Test
    void getProductById_discontinuedProduct_isStillReturned() {
        Product discontinued = new Product(1, "Old Item", List.of(sampleCategory()), new BigDecimal("5.00"), "desc", 0, true);
        when(productRepository.getProductById(1)).thenReturn(Optional.of(discontinued));

        Product result = productUseCaseImpl.getProductById(1);

        assertTrue(result.isDiscontinued());
    }

    // --- updateProduct ---

    @Test
    void updateProduct_productDoesNotExist_throwsProductNotFoundException_andNeverPersists() {
        Product update = persistedProduct(99);
        when(productRepository.getProductById(99)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productUseCaseImpl.updateProduct(update));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void updateProduct_negativePrice_throwsProductNotValidException_andNeverPersists() {
        when(productRepository.getProductById(1)).thenReturn(Optional.of(persistedProduct(1)));

        Product update = new Product(1, "Test", List.of(sampleCategory()), new BigDecimal("-0.01"), "desc", 10, false);

        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.updateProduct(update));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void updateProduct_priceWithMoreThan2Decimals_throwsProductNotValidException_andNeverPersists() {
        when(productRepository.getProductById(1)).thenReturn(Optional.of(persistedProduct(1)));

        Product update = new Product(1, "Test", List.of(sampleCategory()), new BigDecimal("9.999"), "desc", 10, false);

        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.updateProduct(update));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void updateProduct_negativeStock_throwsProductNotValidException_andNeverPersists() {
        when(productRepository.getProductById(1)).thenReturn(Optional.of(persistedProduct(1)));

        Product update = new Product(1, "Test", List.of(sampleCategory()), new BigDecimal("9.99"), "desc", -1, false);

        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.updateProduct(update));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void updateProduct_descriptionExceeds2000Chars_throwsProductNotValidException_andNeverPersists() {
        when(productRepository.getProductById(1)).thenReturn(Optional.of(persistedProduct(1)));

        Product update = new Product(1, "Test", List.of(sampleCategory()), new BigDecimal("9.99"), "x".repeat(2001), 10, false);

        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.updateProduct(update));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void updateProduct_emptyCategories_throwsProductNotValidException_andNeverPersists() {
        when(productRepository.getProductById(1)).thenReturn(Optional.of(persistedProduct(1)));

        Product update = new Product(1, "Test", List.of(), new BigDecimal("9.99"), "desc", 10, false);

        assertThrows(ProductNotValidException.class, () -> productUseCaseImpl.updateProduct(update));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void updateProduct_validProduct_returnsUpdatedProduct() {
        when(productRepository.getProductById(1)).thenReturn(Optional.of(persistedProduct(1)));

        Product update = new Product(1, "Updated Name", List.of(sampleCategory()), new BigDecimal("19.99"), "Updated desc", 20, false);
        when(productRepository.addProduct(any())).thenReturn(update);

        Product result = productUseCaseImpl.updateProduct(update);

        assertEquals("Updated Name", result.getProductName());
        assertEquals(new BigDecimal("19.99"), result.getProductPrice());
        assertEquals(20, result.getProductStock());
    }

    @Test
    void updateProduct_preservesProductId() {
        when(productRepository.getProductById(1)).thenReturn(Optional.of(persistedProduct(1)));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));

        Product update = new Product(1, "Updated Name", List.of(sampleCategory()), new BigDecimal("19.99"), "desc", 10, false);
        Product result = productUseCaseImpl.updateProduct(update);

        assertEquals(1, result.getProductId());
    }

    @Test
    void updateProduct_discontinuedProduct_canBeReactivated() {
        Product discontinued = new Product(1, "Old Item", List.of(sampleCategory()), new BigDecimal("5.00"), "desc", 0, true);
        when(productRepository.getProductById(1)).thenReturn(Optional.of(discontinued));

        Product reactivate = new Product(1, "Old Item", List.of(sampleCategory()), new BigDecimal("5.00"), "desc", 10, false);
        when(productRepository.addProduct(any())).thenReturn(reactivate);

        Product result = productUseCaseImpl.updateProduct(reactivate);

        assertFalse(result.isDiscontinued());
        verify(productRepository, times(1)).addProduct(any());
    }

    // --- removeProduct ---

    @Test
    void removeProduct_nonExistingId_throwsProductNotFoundException_andNeverPersists() {
        when(productRepository.getProductById(99)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productUseCaseImpl.removeProduct(99));
        verify(productRepository, never()).addProduct(any());
    }

    @Test
    void removeProduct_existingProduct_setsDiscontinuedFlagOnPersistedProduct() {
        when(productRepository.getProductById(1)).thenReturn(Optional.of(persistedProduct(1)));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));

        productUseCaseImpl.removeProduct(1);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).addProduct(captor.capture());
        assertTrue(captor.getValue().isDiscontinued());
    }

    @Test
    void removeProduct_existingProduct_doesNotPhysicallyDeleteRow() {
        when(productRepository.getProductById(1)).thenReturn(Optional.of(persistedProduct(1)));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));

        productUseCaseImpl.removeProduct(1);

        // soft delete: a save is issued, no delete operation
        verify(productRepository, times(1)).addProduct(any());
    }

    @Test
    void removeProduct_alreadyDiscontinuedProduct_isIdempotent() {
        Product discontinued = new Product(1, "Old Item", List.of(sampleCategory()), new BigDecimal("9.99"), "desc", 0, true);
        when(productRepository.getProductById(1)).thenReturn(Optional.of(discontinued));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> productUseCaseImpl.removeProduct(1));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).addProduct(captor.capture());
        assertTrue(captor.getValue().isDiscontinued());
    }

    @Test
    void removeProduct_preservesProductDataExceptDiscontinuedFlag() {
        Product existing = new Product(1, "Preserved Item", List.of(sampleCategory()), new BigDecimal("9.99"), "Original desc", 5, false);
        when(productRepository.getProductById(1)).thenReturn(Optional.of(existing));
        when(productRepository.addProduct(any())).thenAnswer(inv -> inv.getArgument(0));

        productUseCaseImpl.removeProduct(1);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).addProduct(captor.capture());
        Product persisted = captor.getValue();
        assertEquals("Preserved Item", persisted.getProductName());
        assertEquals(new BigDecimal("9.99"), persisted.getProductPrice());
        assertEquals("Original desc", persisted.getProductDescription());
        assertEquals(5, persisted.getProductStock());
        assertTrue(persisted.isDiscontinued());
    }
}
