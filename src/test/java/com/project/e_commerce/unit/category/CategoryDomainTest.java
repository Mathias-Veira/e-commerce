package com.project.e_commerce.unit.category;

import com.project.e_commerce.domain.Category;
import com.project.e_commerce.domain.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoryDomainTest {

    // -------------------------------------------------------------------------
    // Name: required, not blank, max 100 characters
    // -------------------------------------------------------------------------

    @Test
    void validateName_acceptsNormalName() {
        assertTrue(Category.validateName("Electronics"));
    }

    @Test
    void validateName_rejectsNull() {
        assertFalse(Category.validateName(null));
    }

    @Test
    void validateName_rejectsEmptyString() {
        assertFalse(Category.validateName(""));
    }

    @Test
    void validateName_rejectsBlankString() {
        assertFalse(Category.validateName("   "));
    }

    @Test
    void validateName_acceptsExactlyMaxLength() {
        assertTrue(Category.validateName("x".repeat(100)));
    }

    @Test
    void validateName_rejectsOverMaxLength() {
        assertFalse(Category.validateName("x".repeat(101)));
    }

    // -------------------------------------------------------------------------
    // Description: null rejected; empty string allowed; max 500 characters
    // -------------------------------------------------------------------------

    @Test
    void validateDescription_acceptsNormalDescription() {
        assertTrue(Category.validateDescription("All electronic devices and accessories."));
    }

    @Test
    void validateDescription_acceptsEmptyString() {
        assertTrue(Category.validateDescription(""));
    }

    @Test
    void validateDescription_acceptsExactlyMaxLength() {
        assertTrue(Category.validateDescription("x".repeat(500)));
    }

    @Test
    void validateDescription_rejectsNull() {
        assertFalse(Category.validateDescription(null));
    }

    @Test
    void validateDescription_rejectsOverMaxLength() {
        assertFalse(Category.validateDescription("x".repeat(501)));
    }

    // -------------------------------------------------------------------------
    // Products: null rejected; empty list valid (category exists independently of products)
    // -------------------------------------------------------------------------

    @Test
    void validateProducts_acceptsEmptyList() {
        assertTrue(Category.validateProducts(Collections.emptyList()));
    }

    @Test
    void validateProducts_acceptsListWithOneProduct() {
        Product p = new Product(1, "Laptop", List.of(), new BigDecimal("999.99"), "A laptop", 10, false);
        assertTrue(Category.validateProducts(List.of(p)));
    }

    @Test
    void validateProducts_acceptsListWithMultipleProducts() {
        Product p1 = new Product(1, "Laptop", List.of(), new BigDecimal("999.99"), "", 10, false);
        Product p2 = new Product(2, "Mouse", List.of(), new BigDecimal("19.99"), "", 50, false);
        assertTrue(Category.validateProducts(List.of(p1, p2)));
    }

    @Test
    void validateProducts_rejectsNull() {
        assertFalse(Category.validateProducts(null));
    }
}
