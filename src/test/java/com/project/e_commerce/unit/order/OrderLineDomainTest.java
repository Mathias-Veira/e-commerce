package com.project.e_commerce.unit.order;

import com.project.e_commerce.domain.OrderLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderLineDomainTest {

    // =========================================================================
    // OrderLine — validateQuantity
    // Min 1, max 100 (anti-typo / anti-abuse guardrail)
    // =========================================================================

    @Test
    void validateQuantity_acceptsMinimum() {
        assertTrue(OrderLine.validateQuantity(1));
    }

    @Test
    void validateQuantity_acceptsMaximum() {
        assertTrue(OrderLine.validateQuantity(100));
    }

    @Test
    void validateQuantity_acceptsValuesInRange() {
        assertTrue(OrderLine.validateQuantity(2));
        assertTrue(OrderLine.validateQuantity(50));
        assertTrue(OrderLine.validateQuantity(99));
    }

    @Test
    void validateQuantity_rejectsZero() {
        assertFalse(OrderLine.validateQuantity(0));
    }

    @Test
    void validateQuantity_rejectsNegative() {
        assertFalse(OrderLine.validateQuantity(-1));
        assertFalse(OrderLine.validateQuantity(-100));
    }

    @Test
    void validateQuantity_rejectsAboveMaximum() {
        assertFalse(OrderLine.validateQuantity(101));
        assertFalse(OrderLine.validateQuantity(500));
    }

    // =========================================================================
    // OrderLine — validateUnitPrice
    // Same rules as Product.productPrice: non-negative, at most 2 decimal places
    // =========================================================================

    @Test
    void validateUnitPrice_acceptsZero() {
        assertTrue(OrderLine.validateUnitPrice(BigDecimal.ZERO));
    }

    @Test
    void validateUnitPrice_acceptsPositiveWithUpToTwoDecimals() {
        assertTrue(OrderLine.validateUnitPrice(new BigDecimal("9.99")));
        assertTrue(OrderLine.validateUnitPrice(new BigDecimal("100")));
        assertTrue(OrderLine.validateUnitPrice(new BigDecimal("0.50")));
        assertTrue(OrderLine.validateUnitPrice(new BigDecimal("9999.00")));
    }

    @Test
    void validateUnitPrice_rejectsNegative() {
        assertFalse(OrderLine.validateUnitPrice(new BigDecimal("-0.01")));
        assertFalse(OrderLine.validateUnitPrice(new BigDecimal("-10")));
    }

    @Test
    void validateUnitPrice_rejectsMoreThanTwoDecimals() {
        assertFalse(OrderLine.validateUnitPrice(new BigDecimal("9.999")));
        assertFalse(OrderLine.validateUnitPrice(new BigDecimal("0.001")));
        assertFalse(OrderLine.validateUnitPrice(new BigDecimal("1.123")));
    }

    @Test
    void validateUnitPrice_rejectsNull() {
        assertFalse(OrderLine.validateUnitPrice(null));
    }
}
