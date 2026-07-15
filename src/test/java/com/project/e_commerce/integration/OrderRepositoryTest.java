package com.project.e_commerce.integration;

import com.project.e_commerce.domain.Order;
import com.project.e_commerce.domain.OrderLine;
import com.project.e_commerce.domain.OrderState;
import com.project.e_commerce.domain.ports.out.OrderRepository;
import com.project.e_commerce.infrastructure.persistence.order.OrderRepositoryJPA;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(OrderRepositoryJPA.class)
class OrderRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private OrderRepository orderRepository;

    private OrderLine buildOrderLine(int productId, String productName, BigDecimal unitPrice, int quantity) {
        return new OrderLine(productId, productName, unitPrice, quantity);
    }

    private Order buildOrder(int userId, List<OrderLine> items, BigDecimal total, OrderState state) {
        return new Order(0, userId, items,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDate.of(2024, 1, 18),
                total, state);
    }

    // ── addOrder ────────────────────────────────────────────────────────────────

    @Test
    void addOrder_ShouldPersistOrderAndReturnWithGeneratedId() {
        OrderLine line = buildOrderLine(1, "Laptop", new BigDecimal("999.99"), 2);
        Order saved = orderRepository.addOrder(buildOrder(1, List.of(line), new BigDecimal("1999.98"), OrderState.CONFIRMED));

        assertThat(saved.getOrderId()).isPositive();
        assertThat(saved.getUserId()).isEqualTo(1);
        assertThat(saved.getOrderTotal()).isEqualByComparingTo(new BigDecimal("1999.98"));
        assertThat(saved.getOrderState()).isEqualTo(OrderState.CONFIRMED);
        assertThat(saved.getItems()).hasSize(1);
    }

    @Test
    void addOrder_ShouldBeRetrievableAfterPersisting() {
        OrderLine line = buildOrderLine(1, "Phone", new BigDecimal("499.99"), 1);
        Order saved = orderRepository.addOrder(buildOrder(2, List.of(line), new BigDecimal("499.99"), OrderState.CONFIRMED));

        assertThat(orderRepository.getAllOrders())
                .extracting(Order::getOrderId)
                .contains(saved.getOrderId());
    }

    @Test
    void addOrder_EachOrderShouldReceiveUniqueId() {
        OrderLine line = buildOrderLine(1, "Item", new BigDecimal("10.00"), 1);
        Order first = orderRepository.addOrder(buildOrder(1, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));
        Order second = orderRepository.addOrder(buildOrder(1, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));

        assertThat(first.getOrderId()).isNotEqualTo(second.getOrderId());
    }

    @Test
    void addOrder_ShouldPersistAllFields() {
        LocalDateTime orderDate = LocalDateTime.of(2024, 3, 10, 9, 30);
        LocalDate deliveryDate = LocalDate.of(2024, 3, 13);
        OrderLine line = buildOrderLine(5, "Monitor", new BigDecimal("299.99"), 1);
        Order order = new Order(0, 7, List.of(line), orderDate, deliveryDate, new BigDecimal("299.99"), OrderState.CONFIRMED);

        Order saved = orderRepository.addOrder(order);
        Order retrieved = orderRepository.getAllOrders().stream()
                .filter(o -> o.getOrderId() == saved.getOrderId())
                .findFirst().orElseThrow();

        assertThat(retrieved.getUserId()).isEqualTo(7);
        assertThat(retrieved.getOrderDate()).isEqualTo(orderDate);
        assertThat(retrieved.getDeliveryDate()).isEqualTo(deliveryDate);
        assertThat(retrieved.getOrderTotal()).isEqualByComparingTo(new BigDecimal("299.99"));
        assertThat(retrieved.getOrderState()).isEqualTo(OrderState.CONFIRMED);
    }

    @Test
    void addOrder_WithMultipleLines_ShouldPersistAllLines() {
        OrderLine line1 = buildOrderLine(1, "Laptop", new BigDecimal("999.99"), 1);
        OrderLine line2 = buildOrderLine(2, "Mouse", new BigDecimal("29.99"), 2);
        OrderLine line3 = buildOrderLine(3, "Keyboard", new BigDecimal("79.99"), 1);
        Order saved = orderRepository.addOrder(buildOrder(1, List.of(line1, line2, line3), new BigDecimal("1139.96"), OrderState.CONFIRMED));

        assertThat(saved.getItems()).hasSize(3);
    }

    @Test
    void addOrder_ShouldPersistAllOrderLineFields() {
        OrderLine line = buildOrderLine(42, "Gaming Chair", new BigDecimal("349.99"), 3);
        Order saved = orderRepository.addOrder(buildOrder(1, List.of(line), new BigDecimal("1049.97"), OrderState.CONFIRMED));

        OrderLine savedLine = saved.getItems().get(0);
        assertThat(savedLine.getProductId()).isEqualTo(42);
        assertThat(savedLine.getProductName()).isEqualTo("Gaming Chair");
        assertThat(savedLine.getProductUnitPrice()).isEqualByComparingTo(new BigDecimal("349.99"));
        assertThat(savedLine.getQuantity()).isEqualTo(3);
    }

    @Test
    void addOrder_ShouldPersistWithCancelledState() {
        OrderLine line = buildOrderLine(1, "Tablet", new BigDecimal("599.99"), 1);
        Order saved = orderRepository.addOrder(buildOrder(3, List.of(line), new BigDecimal("599.99"), OrderState.CANCELLED));

        assertThat(saved.getOrderState()).isEqualTo(OrderState.CANCELLED);
    }

    // ── getAllOrders ─────────────────────────────────────────────────────────────

    @Test
    void getAllOrders_WithEmptyDatabase_ShouldReturnEmptyList() {
        assertThat(orderRepository.getAllOrders()).isEmpty();
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        OrderLine line = buildOrderLine(1, "Item", new BigDecimal("10.00"), 1);
        orderRepository.addOrder(buildOrder(1, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));
        orderRepository.addOrder(buildOrder(2, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));
        orderRepository.addOrder(buildOrder(3, List.of(line), new BigDecimal("10.00"), OrderState.CANCELLED));

        assertThat(orderRepository.getAllOrders()).hasSize(3);
    }

    @Test
    void getAllOrders_ShouldReturnOrdersWithTheirLines() {
        OrderLine line1 = buildOrderLine(1, "Headset", new BigDecimal("99.99"), 1);
        OrderLine line2 = buildOrderLine(2, "Webcam", new BigDecimal("49.99"), 2);
        orderRepository.addOrder(buildOrder(1, List.of(line1, line2), new BigDecimal("199.97"), OrderState.CONFIRMED));

        List<Order> result = orderRepository.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItems()).hasSize(2);
    }

    @Test
    void getAllOrders_ShouldReturnOrdersFromDifferentUsers() {
        OrderLine line = buildOrderLine(1, "Item", new BigDecimal("10.00"), 1);
        orderRepository.addOrder(buildOrder(10, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));
        orderRepository.addOrder(buildOrder(20, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));

        List<Order> result = orderRepository.getAllOrders();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Order::getUserId).containsExactlyInAnyOrder(10, 20);
    }

    // ── getOrdersByUserId ────────────────────────────────────────────────────────

    @Test
    void getOrdersByUserId_WhenUserHasOrders_ShouldReturnMatchingOrders() {
        OrderLine line = buildOrderLine(1, "Item", new BigDecimal("10.00"), 1);
        orderRepository.addOrder(buildOrder(5, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));
        orderRepository.addOrder(buildOrder(5, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));

        List<Order> result = orderRepository.getOrdersByUserId(5);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(o -> assertThat(o.getUserId()).isEqualTo(5));
    }

    @Test
    void getOrdersByUserId_WhenUserHasNoOrders_ShouldReturnEmptyList() {
        assertThat(orderRepository.getOrdersByUserId(999)).isEmpty();
    }

    @Test
    void getOrdersByUserId_WithEmptyDatabase_ShouldReturnEmptyList() {
        assertThat(orderRepository.getOrdersByUserId(1)).isEmpty();
    }

    @Test
    void getOrdersByUserId_ShouldNotReturnOrdersFromOtherUsers() {
        OrderLine line = buildOrderLine(1, "Item", new BigDecimal("10.00"), 1);
        orderRepository.addOrder(buildOrder(1, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));
        orderRepository.addOrder(buildOrder(2, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));
        orderRepository.addOrder(buildOrder(3, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));

        List<Order> result = orderRepository.getOrdersByUserId(2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(2);
    }

    @Test
    void getOrdersByUserId_ShouldReturnOrdersWithTheirLines() {
        OrderLine line1 = buildOrderLine(1, "Laptop", new BigDecimal("999.99"), 1);
        OrderLine line2 = buildOrderLine(2, "Mouse", new BigDecimal("29.99"), 1);
        orderRepository.addOrder(buildOrder(8, List.of(line1, line2), new BigDecimal("1029.98"), OrderState.CONFIRMED));

        List<Order> result = orderRepository.getOrdersByUserId(8);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItems()).hasSize(2);
    }

    // ── getOrdersByProductId ─────────────────────────────────────────────────────

    @Test
    void getOrdersByProductId_WhenOrderContainsProduct_ShouldReturnOrder() {
        OrderLine line = buildOrderLine(100, "TV", new BigDecimal("799.99"), 1);
        Order saved = orderRepository.addOrder(buildOrder(1, List.of(line), new BigDecimal("799.99"), OrderState.CONFIRMED));

        List<Order> result = orderRepository.getOrdersByProductId(100);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(saved.getOrderId());
    }

    @Test
    void getOrdersByProductId_WhenNoOrderContainsProduct_ShouldReturnEmptyList() {
        assertThat(orderRepository.getOrdersByProductId(999)).isEmpty();
    }

    @Test
    void getOrdersByProductId_WithEmptyDatabase_ShouldReturnEmptyList() {
        assertThat(orderRepository.getOrdersByProductId(1)).isEmpty();
    }

    @Test
    void getOrdersByProductId_ShouldNotReturnOrdersWithDifferentProduct() {
        orderRepository.addOrder(buildOrder(1,
                List.of(buildOrderLine(10, "Phone", new BigDecimal("499.99"), 1)),
                new BigDecimal("499.99"), OrderState.CONFIRMED));
        orderRepository.addOrder(buildOrder(2,
                List.of(buildOrderLine(20, "Case", new BigDecimal("19.99"), 1)),
                new BigDecimal("19.99"), OrderState.CONFIRMED));

        List<Order> result = orderRepository.getOrdersByProductId(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItems()).extracting(OrderLine::getProductId).contains(10);
    }

    @Test
    void getOrdersByProductId_WhenMultipleOrdersContainSameProduct_ShouldReturnAll() {
        OrderLine line = buildOrderLine(50, "Charger", new BigDecimal("29.99"), 1);
        orderRepository.addOrder(buildOrder(1, List.of(line), new BigDecimal("29.99"), OrderState.CONFIRMED));
        orderRepository.addOrder(buildOrder(2, List.of(line), new BigDecimal("29.99"), OrderState.CONFIRMED));
        orderRepository.addOrder(buildOrder(3, List.of(line), new BigDecimal("29.99"), OrderState.CANCELLED));

        List<Order> result = orderRepository.getOrdersByProductId(50);

        assertThat(result).hasSize(3);
    }

    @Test
    void getOrdersByProductId_WhenProductIsOneOfManyLines_ShouldReturnOrder() {
        OrderLine line1 = buildOrderLine(1, "Laptop", new BigDecimal("999.99"), 1);
        OrderLine line2 = buildOrderLine(2, "Bag", new BigDecimal("49.99"), 1);
        OrderLine line3 = buildOrderLine(3, "Mouse", new BigDecimal("29.99"), 1);
        Order saved = orderRepository.addOrder(buildOrder(1, List.of(line1, line2, line3), new BigDecimal("1079.97"), OrderState.CONFIRMED));

        List<Order> result = orderRepository.getOrdersByProductId(2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(saved.getOrderId());
    }

    // ── getOrderById ────────────────────────────────────────────────────────────

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        OrderLine line = buildOrderLine(1, "Laptop", new BigDecimal("999.99"), 1);
        Order saved = orderRepository.addOrder(buildOrder(1, List.of(line), new BigDecimal("999.99"), OrderState.CONFIRMED));

        assertThat(orderRepository.getOrderById(saved.getOrderId())).isPresent();
        assertThat(orderRepository.getOrderById(saved.getOrderId()).get().getOrderId()).isEqualTo(saved.getOrderId());
    }

    @Test
    void getOrderById_WhenOrderDoesNotExist_ShouldReturnEmpty() {
        assertThat(orderRepository.getOrderById(999999)).isEmpty();
    }

    @Test
    void getOrderById_WithEmptyDatabase_ShouldReturnEmpty() {
        assertThat(orderRepository.getOrderById(1)).isEmpty();
    }

    @Test
    void getOrderById_ShouldReturnOrderWithItsLines() {
        OrderLine line1 = buildOrderLine(1, "Laptop", new BigDecimal("999.99"), 1);
        OrderLine line2 = buildOrderLine(2, "Mouse", new BigDecimal("29.99"), 2);
        Order saved = orderRepository.addOrder(buildOrder(1, List.of(line1, line2), new BigDecimal("1059.97"), OrderState.CONFIRMED));

        assertThat(orderRepository.getOrderById(saved.getOrderId()))
                .isPresent()
                .get()
                .satisfies(o -> assertThat(o.getItems()).hasSize(2));
    }

    @Test
    void getOrderById_ShouldReturnCorrectOrderAmongMultiple() {
        OrderLine line = buildOrderLine(1, "Item", new BigDecimal("10.00"), 1);
        Order first = orderRepository.addOrder(buildOrder(1, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));
        Order second = orderRepository.addOrder(buildOrder(2, List.of(line), new BigDecimal("10.00"), OrderState.CONFIRMED));

        assertThat(orderRepository.getOrderById(first.getOrderId()))
                .isPresent()
                .get()
                .satisfies(o -> {
                    assertThat(o.getOrderId()).isEqualTo(first.getOrderId());
                    assertThat(o.getUserId()).isEqualTo(1);
                    assertThat(o.getOrderId()).isNotEqualTo(second.getOrderId());
                });
    }

    @Test
    void getOrderById_ShouldReturnOrderWithAllFields() {
        LocalDateTime orderDate = LocalDateTime.of(2024, 5, 20, 14, 30);
        LocalDate deliveryDate = LocalDate.of(2024, 5, 23);
        OrderLine line = buildOrderLine(7, "Keyboard", new BigDecimal("79.99"), 2);
        Order order = new Order(0, 3, List.of(line), orderDate, deliveryDate, new BigDecimal("159.98"), OrderState.CONFIRMED);
        Order saved = orderRepository.addOrder(order);

        Order retrieved = orderRepository.getOrderById(saved.getOrderId()).orElseThrow();

        assertThat(retrieved.getUserId()).isEqualTo(3);
        assertThat(retrieved.getOrderDate()).isEqualTo(orderDate);
        assertThat(retrieved.getDeliveryDate()).isEqualTo(deliveryDate);
        assertThat(retrieved.getOrderTotal()).isEqualByComparingTo(new BigDecimal("159.98"));
        assertThat(retrieved.getOrderState()).isEqualTo(OrderState.CONFIRMED);
    }
}
