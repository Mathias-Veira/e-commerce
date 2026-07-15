package com.project.e_commerce.unit.order;

import com.project.e_commerce.domain.Order;
import com.project.e_commerce.domain.OrderLine;
import com.project.e_commerce.domain.OrderState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDomainTest {

    // =========================================================================
    // Order — validateHasLines
    // An order must contain at least one line; empty / null rejected
    // =========================================================================

    @Test
    void validateHasLines_acceptsSingleLine() {
        OrderLine line = new OrderLine(1, "Product A", new BigDecimal("10.00"), 2);
        assertTrue(Order.validateHasLines(List.of(line)));
    }

    @Test
    void validateHasLines_acceptsMultipleLines() {
        OrderLine line1 = new OrderLine(1, "Product A", new BigDecimal("10.00"), 2);
        OrderLine line2 = new OrderLine(2, "Product B", new BigDecimal("5.00"), 1);
        assertTrue(Order.validateHasLines(List.of(line1, line2)));
    }

    @Test
    void validateHasLines_rejectsEmptyList() {
        assertFalse(Order.validateHasLines(List.of()));
    }

    @Test
    void validateHasLines_rejectsNull() {
        assertFalse(Order.validateHasLines(null));
    }

    // =========================================================================
    // Order — computeTotal
    // Sum of (unitPrice × quantity) for every line, computed at confirmation time
    // =========================================================================

    @Test
    void computeTotal_singleLine() {
        // 10.00 × 3 = 30.00
        Order order = new Order(0, 1,
            List.of(new OrderLine(1, "Product A", new BigDecimal("10.00"), 3)),
            null, null, null, OrderState.CONFIRMED);
        order.computeTotal();
        assertEquals(new BigDecimal("30.00"), order.getOrderTotal());
    }

    @Test
    void computeTotal_multipleLines() {
        // 10.00×2 + 5.50×4 = 20.00 + 22.00 = 42.00
        Order order = new Order(0, 1, List.of(
            new OrderLine(1, "Product A", new BigDecimal("10.00"), 2),
            new OrderLine(2, "Product B", new BigDecimal("5.50"), 4)
        ), null, null, null, OrderState.CONFIRMED);
        order.computeTotal();
        assertEquals(new BigDecimal("42.00"), order.getOrderTotal());
    }

    @Test
    void computeTotal_singleLineWithQuantityOne() {
        // price × 1 = price
        Order order = new Order(0, 1,
            List.of(new OrderLine(1, "Product A", new BigDecimal("19.99"), 1)),
            null, null, null, OrderState.CONFIRMED);
        order.computeTotal();
        assertEquals(new BigDecimal("19.99"), order.getOrderTotal());
    }

    // =========================================================================
    // Order — computeDeliveryDate
    // confirmationDate + 3 days; if that lands on Saturday or Sunday → next Monday
    // Uses all seven starting days to cover every case
    // =========================================================================

    @Test
    void computeDeliveryDate_mondayConfirmation_returnsThursday() {
        // 2026-07-06 (Mon) + 3 = 2026-07-09 (Thu)
        LocalDateTime monday = LocalDateTime.of(2026, 7, 6, 10, 0);
        assertEquals(LocalDate.of(2026, 7, 9), Order.computeDeliveryDate(monday));
    }

    @Test
    void computeDeliveryDate_tuesdayConfirmation_returnsFriday() {
        // 2026-07-07 (Tue) + 3 = 2026-07-10 (Fri)
        LocalDateTime tuesday = LocalDateTime.of(2026, 7, 7, 10, 0);
        assertEquals(LocalDate.of(2026, 7, 10), Order.computeDeliveryDate(tuesday));
    }

    @Test
    void computeDeliveryDate_wednesdayConfirmation_skipsToMonday() {
        // 2026-07-08 (Wed) + 3 = 2026-07-11 (Sat) → pushed to 2026-07-13 (Mon)
        LocalDateTime wednesday = LocalDateTime.of(2026, 7, 8, 10, 0);
        assertEquals(LocalDate.of(2026, 7, 13), Order.computeDeliveryDate(wednesday));
    }

    @Test
    void computeDeliveryDate_thursdayConfirmation_skipsToMonday() {
        // 2026-07-09 (Thu) + 3 = 2026-07-12 (Sun) → pushed to 2026-07-13 (Mon)
        LocalDateTime thursday = LocalDateTime.of(2026, 7, 9, 10, 0);
        assertEquals(LocalDate.of(2026, 7, 13), Order.computeDeliveryDate(thursday));
    }

    @Test
    void computeDeliveryDate_fridayConfirmation_returnsMonday() {
        // 2026-07-10 (Fri) + 3 = 2026-07-13 (Mon) — already Monday, no push needed
        LocalDateTime friday = LocalDateTime.of(2026, 7, 10, 10, 0);
        assertEquals(LocalDate.of(2026, 7, 13), Order.computeDeliveryDate(friday));
    }

    @Test
    void computeDeliveryDate_saturdayConfirmation_returnsTuesday() {
        // 2026-07-11 (Sat) + 3 = 2026-07-14 (Tue) — weekday, no push
        LocalDateTime saturday = LocalDateTime.of(2026, 7, 11, 10, 0);
        assertEquals(LocalDate.of(2026, 7, 14), Order.computeDeliveryDate(saturday));
    }

    @Test
    void computeDeliveryDate_sundayConfirmation_returnsWednesday() {
        // 2026-07-12 (Sun) + 3 = 2026-07-15 (Wed) — weekday, no push
        LocalDateTime sunday = LocalDateTime.of(2026, 7, 12, 10, 0);
        assertEquals(LocalDate.of(2026, 7, 15), Order.computeDeliveryDate(sunday));
    }

    // =========================================================================
    // Order — isCancellable
    // Only a CONFIRMADO order can be cancelled, and only within 1 hour of its
    // confirmation; exactly at the 1-hour mark it is no longer cancellable.
    // An already-CANCELADO order can never be cancelled again.
    // =========================================================================

    @Test
    void isCancellable_justConfirmed_returnsTrue() {
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 7, 6, 10, 0);
        Order order = new Order(1, 1, List.of(), confirmedAt, null, BigDecimal.TEN, OrderState.CONFIRMED);
        assertTrue(order.isCancellable(confirmedAt.plusSeconds(1)));
    }

    @Test
    void isCancellable_withinOneHour_returnsTrue() {
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 7, 6, 10, 0);
        Order order = new Order(1, 1, List.of(), confirmedAt, null, BigDecimal.TEN, OrderState.CONFIRMED);
        assertTrue(order.isCancellable(confirmedAt.plusMinutes(59).plusSeconds(59)));
    }

    @Test
    void isCancellable_exactlyOneHourLater_returnsFalse() {
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 7, 6, 10, 0);
        Order order = new Order(1, 1, List.of(), confirmedAt, null, BigDecimal.TEN, OrderState.CONFIRMED);
        assertFalse(order.isCancellable(confirmedAt.plusHours(1)));
    }

    @Test
    void isCancellable_afterOneHour_returnsFalse() {
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 7, 6, 10, 0);
        Order order = new Order(1, 1, List.of(), confirmedAt, null, BigDecimal.TEN, OrderState.CONFIRMED);
        assertFalse(order.isCancellable(confirmedAt.plusHours(2)));
    }

    @Test
    void isCancellable_alreadyCancelled_returnsFalse() {
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 7, 6, 10, 0);
        Order order = new Order(1, 1, List.of(), confirmedAt, null, BigDecimal.TEN, OrderState.CANCELLED);
        assertFalse(order.isCancellable(confirmedAt.plusSeconds(1)));
    }

    // =========================================================================
    // Order — mergeLines (confirmation-time normalization)
    // The cart is not persisted; at confirmation the incoming lines are merged
    // so that duplicate productIds collapse into a single line with the summed
    // quantity (invariant: never two lines for the same productId in one order),
    // while distinct products stay as separate lines.
    // =========================================================================

    @Test
    void mergeLines_distinctProductIds_keepsSeparateLines() {
        List<OrderLine> merged = Order.mergeLines(List.of(
            new OrderLine(1, "Product A", new BigDecimal("10.00"), 2),
            new OrderLine(2, "Product B", new BigDecimal("5.00"), 1)
        ));
        assertEquals(2, merged.size());
    }

    @Test
    void mergeLines_duplicateProductId_collapsesToSingleLine() {
        List<OrderLine> merged = Order.mergeLines(List.of(
            new OrderLine(1, "Product A", new BigDecimal("10.00"), 2),
            new OrderLine(1, "Product A", new BigDecimal("10.00"), 3)
        ));
        assertEquals(1, merged.size());
    }

    @Test
    void mergeLines_duplicateProductId_sumsQuantities() {
        List<OrderLine> merged = Order.mergeLines(List.of(
            new OrderLine(1, "Product A", new BigDecimal("10.00"), 2),
            new OrderLine(1, "Product A", new BigDecimal("10.00"), 3)
        ));
        assertEquals(5, merged.get(0).getQuantity());
    }

    @Test
    void mergeLines_mergesOnlyTheDuplicateProductId() {
        // Product A appears twice → merged; Product B stays separate
        List<OrderLine> merged = Order.mergeLines(List.of(
            new OrderLine(1, "Product A", new BigDecimal("10.00"), 1),
            new OrderLine(2, "Product B", new BigDecimal("5.00"), 1),
            new OrderLine(1, "Product A", new BigDecimal("10.00"), 2)
        ));

        assertEquals(2, merged.size());

        OrderLine lineA = merged.stream()
            .filter(l -> l.getProductId() == 1)
            .findFirst()
            .orElseThrow();
        assertEquals(3, lineA.getQuantity());
    }
}
