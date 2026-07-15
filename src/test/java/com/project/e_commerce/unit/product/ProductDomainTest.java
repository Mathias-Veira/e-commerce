package com.project.e_commerce.unit.product;

import com.project.e_commerce.domain.Category;
import com.project.e_commerce.domain.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDomainTest {

    // -------------------------------------------------------------------------
    // Price: negative blocked, zero valid, max 2 decimals enforced (no rounding)
    // -------------------------------------------------------------------------

    @Test
    void validatePrice_acceptsZero() {
        assertTrue(Product.validatePrice(BigDecimal.ZERO));
    }

    @Test
    void validatePrice_acceptsPositiveWithUpToTwoDecimals() {
        assertTrue(Product.validatePrice(new BigDecimal("10")));
        assertTrue(Product.validatePrice(new BigDecimal("10.5")));
        assertTrue(Product.validatePrice(new BigDecimal("10.99")));
        assertTrue(Product.validatePrice(new BigDecimal("9999.00")));
    }

    @Test
    void validatePrice_rejectsNegative() {
        assertFalse(Product.validatePrice(new BigDecimal("-0.01")));
        assertFalse(Product.validatePrice(new BigDecimal("-100")));
    }

    @Test
    void validatePrice_rejectsMoreThanTwoDecimals() {
        assertFalse(Product.validatePrice(new BigDecimal("10.001")));
        assertFalse(Product.validatePrice(new BigDecimal("0.123")));
        assertFalse(Product.validatePrice(new BigDecimal("9.9999")));
    }

    @Test
    void validatePrice_rejectsNull() {
        assertFalse(Product.validatePrice(null));
    }

    // -------------------------------------------------------------------------
    // Stock: negative blocked; zero is "out of stock" but a valid state
    // -------------------------------------------------------------------------

    @Test
    void validateStock_acceptsZero() {
        assertTrue(Product.validateStock(0));
    }

    @Test
    void validateStock_acceptsPositive() {
        assertTrue(Product.validateStock(1));
        assertTrue(Product.validateStock(1000));
    }

    @Test
    void validateStock_rejectsNegative() {
        assertFalse(Product.validateStock(-1));
        assertFalse(Product.validateStock(-500));
    }

    // -------------------------------------------------------------------------
    // Description: null rejected; empty string allowed; max 2000 characters
    // -------------------------------------------------------------------------

    @Test
    void validateDescription_acceptsEmptyString() {
        assertTrue(Product.validateDescription(""));
    }

    @Test
    void validateDescription_acceptsNormalText() {
        assertTrue(Product.validateDescription("A great product."));
    }

    @Test
    void validateDescription_acceptsExactlyMaxLength() {
        assertTrue(Product.validateDescription("x".repeat(2000)));
    }

    @Test
    void validateDescription_rejectsNull() {
        assertFalse(Product.validateDescription(null));
    }

    @Test
    void validateDescription_rejectsOverMaxLength() {
        assertFalse(Product.validateDescription("x".repeat(2001)));
    }

    // -------------------------------------------------------------------------
    // Categories: product must have at least one category
    // -------------------------------------------------------------------------

    @Test
    void validateCategories_acceptsSingleCategory() {
        Category cat = new Category(1, "Electronics", "Electronic products", List.of());
        assertTrue(Product.validateCategories(List.of(cat)));
    }

    @Test
    void validateCategories_acceptsMultipleCategories() {
        Category cat1 = new Category(1, "Electronics", "", List.of());
        Category cat2 = new Category(2, "Phones", "", List.of());
        assertTrue(Product.validateCategories(List.of(cat1, cat2)));
    }

    @Test
    void validateCategories_rejectsEmptyList() {
        assertFalse(Product.validateCategories(Collections.emptyList()));
    }

    @Test
    void validateCategories_rejectsNull() {
        assertFalse(Product.validateCategories(null));
    }

    // -------------------------------------------------------------------------
    // isOutOfStock: derived from productStock == 0; never stored separately
    // -------------------------------------------------------------------------

    @Test
    void isOutOfStock_trueWhenStockIsZero() {
        Product p = new Product(0, "Item", List.of(), BigDecimal.ONE, "", 0, false);
        assertTrue(p.isOutOfStock());
    }

    @Test
    void isOutOfStock_falseWhenStockIsPositive() {
        Product p = new Product(0, "Item", List.of(), BigDecimal.ONE, "", 5, false);
        assertFalse(p.isOutOfStock());
    }

    // -------------------------------------------------------------------------
    // Discontinued: boolean flag, independent from stock state
    // -------------------------------------------------------------------------

    @Test
    void discontinued_isIndependentFromStock_canBeInStockAndDiscontinued() {
        // A product with stock > 0 can still be marked as discontinued
        Product p = new Product(0, "Old Item", List.of(), BigDecimal.ONE, "", 10, true);
        assertTrue(p.isDiscontinued());
        assertFalse(p.isOutOfStock());
    }

    @Test
    void discontinued_isIndependentFromStock_canBeOutOfStockAndNotDiscontinued() {
        // A product with stock == 0 is not automatically discontinued
        Product p = new Product(0, "Temp Item", List.of(), BigDecimal.ONE, "", 0, false);
        assertFalse(p.isDiscontinued());
        assertTrue(p.isOutOfStock());
    }

    @Test
    void newProduct_isNotDiscontinuedByDefault() {
        Product p = new Product(0, "New Item", List.of(), new BigDecimal("5.00"), "", 100, false);
        assertFalse(p.isDiscontinued());
    }
}
