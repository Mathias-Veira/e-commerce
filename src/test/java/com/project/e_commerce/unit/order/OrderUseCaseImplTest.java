package com.project.e_commerce.unit.order;

import com.project.e_commerce.application.OrderUseCaseImpl;
import com.project.e_commerce.application.exceptions.order.OrderNotFoundException;
import com.project.e_commerce.application.exceptions.product.ProductNotFoundException;
import com.project.e_commerce.application.exceptions.user.UserNotFoundException;
import com.project.e_commerce.domain.Order;
import com.project.e_commerce.domain.OrderLine;
import com.project.e_commerce.domain.OrderState;
import com.project.e_commerce.domain.Product;
import com.project.e_commerce.domain.User;
import com.project.e_commerce.domain.exceptions.OrderNotValidException;
import com.project.e_commerce.domain.ports.out.OrderRepository;
import com.project.e_commerce.domain.ports.out.ProductRepository;
import com.project.e_commerce.domain.ports.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;

    private OrderUseCaseImpl orderUseCaseImpl;

    @BeforeEach
    void setUp() {
        orderUseCaseImpl = new OrderUseCaseImpl(orderRepository,userRepository,productRepository);
    }

    private OrderLine line(int productId, String name, BigDecimal price, int quantity) {
        return new OrderLine(productId, name, price, quantity);
    }

    private OrderLine validLine() {
        return new OrderLine(1, "Laptop", new BigDecimal("999.99"), 1);
    }

    private Order incomingOrder(int userId, List<OrderLine> lines) {
        return new Order(0, userId, lines, null, null, null, null);
    }

    private User existingUser(int id) {
        return new User(id, "user@example.com", "username", "hashedPassword");
    }

    private Product existingProduct(int id) {
        return new Product(id, "Product", List.of(), new BigDecimal("9.99"), "desc", 10, false);
    }

    private Order persistedOrder(int orderId, int userId, LocalDateTime orderDate) {
        return new Order(orderId, userId, List.of(validLine()),
                orderDate,
                orderDate.toLocalDate().plusDays(3),
                new BigDecimal("999.99"),
                OrderState.CONFIRMED);
    }

    // --- addOrder ---

    @Test
    void addOrder_nullLines_throwsOrderNotValidException_andNeverPersists() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, null);
        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    void addOrder_emptyLines_throwsOrderNotValidException_andNeverPersists() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of());
        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    void addOrder_lineWithZeroQuantity_throwsOrderNotValidException_andNeverPersists() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(line(1, "Laptop", new BigDecimal("999.99"), 0)));
        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    void addOrder_lineWithNegativeQuantity_throwsOrderNotValidException_andNeverPersists() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(line(1, "Laptop", new BigDecimal("999.99"), -1)));
        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    void addOrder_lineWithQuantityOver100_throwsOrderNotValidException_andNeverPersists() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(line(1, "Laptop", new BigDecimal("999.99"), 101)));
        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    void addOrder_lineWithNullPrice_throwsOrderNotValidException_andNeverPersists() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(line(1, "Laptop", null, 1)));
        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    void addOrder_lineWithNegativePrice_throwsOrderNotValidException_andNeverPersists() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(line(1, "Laptop", new BigDecimal("-0.01"), 1)));
        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    void addOrder_lineWithPriceMoreThan2Decimals_throwsOrderNotValidException_andNeverPersists() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(line(1, "Laptop", new BigDecimal("9.999"), 1)));
        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    void addOrder_validOrder_setsStateToConfirmed() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(validLine()));
        when(productRepository.getProductById(1)).thenReturn(Optional.of(existingProduct(1)));
        when(orderRepository.addOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        orderUseCaseImpl.addOrder(order);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).addOrder(captor.capture());
        assertEquals(OrderState.CONFIRMED, captor.getValue().getOrderState());
    }

    @Test
    void addOrder_validOrder_setsOrderDateBeforePersisting() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(validLine()));
        when(productRepository.getProductById(1)).thenReturn(Optional.of(existingProduct(1)));
        when(orderRepository.addOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        orderUseCaseImpl.addOrder(order);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).addOrder(captor.capture());
        assertNotNull(captor.getValue().getOrderDate());
    }

    @Test
    void addOrder_validOrder_setsDeliveryDateBeforePersisting() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(validLine()));
        when(productRepository.getProductById(1)).thenReturn(Optional.of(existingProduct(1)));
        when(orderRepository.addOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        orderUseCaseImpl.addOrder(order);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).addOrder(captor.capture());
        assertNotNull(captor.getValue().getDeliveryDate());
    }

    @Test
    void addOrder_validOrder_computesTotalBeforePersisting() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        OrderLine line1 = line(1, "Laptop", new BigDecimal("999.99"), 2);
        OrderLine line2 = line(2, "Mouse", new BigDecimal("29.99"), 3);
        Order order = incomingOrder(1, List.of(line1, line2));
        when(productRepository.getProductById(1)).thenReturn(Optional.of(new Product(1, "Laptop", List.of(), new BigDecimal("999.99"), "desc", 10, false)));
        when(productRepository.getProductById(2)).thenReturn(Optional.of(new Product(2, "Mouse", List.of(), new BigDecimal("29.99"), "desc", 10, false)));
        when(orderRepository.addOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        orderUseCaseImpl.addOrder(order);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).addOrder(captor.capture());
        BigDecimal expectedTotal = new BigDecimal("999.99").multiply(BigDecimal.valueOf(2))
                .add(new BigDecimal("29.99").multiply(BigDecimal.valueOf(3)));
        assertEquals(0, expectedTotal.compareTo(captor.getValue().getOrderTotal()));
    }

    @Test
    void addOrder_duplicateProductLines_mergesIntoSingleLineBeforePersisting() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        OrderLine line1 = line(1, "Laptop", new BigDecimal("999.99"), 1);
        OrderLine line2 = line(1, "Laptop", new BigDecimal("999.99"), 2);
        Order order = incomingOrder(1, List.of(line1, line2));
        when(productRepository.getProductById(1)).thenReturn(Optional.of(existingProduct(1)));
        when(orderRepository.addOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        orderUseCaseImpl.addOrder(order);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).addOrder(captor.capture());
        assertEquals(1, captor.getValue().getItems().size());
        assertEquals(3, captor.getValue().getItems().get(0).getQuantity());
    }

    @Test
    void addOrder_validOrder_returnsOrderFromRepository() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(validLine()));
        Order saved = persistedOrder(42, 1, LocalDateTime.now());
        when(productRepository.getProductById(1)).thenReturn(Optional.of(existingProduct(1)));
        when(orderRepository.addOrder(any())).thenReturn(saved);

        Order result = orderUseCaseImpl.addOrder(order);

        assertEquals(42, result.getOrderId());
    }

    @Test
    void addOrder_validOrder_callsRemoveStockForEachLine() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        OrderLine line1 = line(1, "Laptop", new BigDecimal("999.99"), 2);
        OrderLine line2 = line(2, "Mouse", new BigDecimal("29.99"), 3);
        Order order = incomingOrder(1, List.of(line1, line2));
        when(productRepository.getProductById(1)).thenReturn(Optional.of(existingProduct(1)));
        when(productRepository.getProductById(2)).thenReturn(Optional.of(existingProduct(2)));
        when(orderRepository.addOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        orderUseCaseImpl.addOrder(order);

        verify(productRepository).removeStock(1, 2);
        verify(productRepository).removeStock(2, 3);
    }

    @Test
    void addOrder_lineWithInvalidQuantity_neverCallsRemoveStock() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(line(1, "Laptop", new BigDecimal("999.99"), 0)));

        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(productRepository, never()).removeStock(anyInt(), anyInt());
    }

    @Test
    void addOrder_lineWithInvalidPrice_neverCallsRemoveStock() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = incomingOrder(1, List.of(line(1, "Laptop", new BigDecimal("-1.00"), 1)));

        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(productRepository, never()).removeStock(anyInt(), anyInt());
    }
    @Test
    void addOrder_discontinuedProduct_throwsOrderNotValidException() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        Order order = new Order(0, 1, List.of(validLine()), null, null, null, OrderState.CONFIRMED);
        Product discontinued = new Product(1, "Laptop", List.of(), new BigDecimal("999.99"), "desc", 10, true);
        when(productRepository.getProductById(1)).thenReturn(Optional.of(discontinued));

        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(productRepository, never()).removeStock(anyInt(), anyInt());
    }

    @Test
    void addOrder_insufficientStock_throwsOrderNotValidException() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        OrderLine lineWith5 = line(1, "Laptop", new BigDecimal("999.99"), 5);
        Order order = new Order(0, 1, List.of(lineWith5), null, null, null, OrderState.CONFIRMED);
        Product lowStock = new Product(1, "Laptop", List.of(), new BigDecimal("999.99"), "desc", 3, false);
        when(productRepository.getProductById(1)).thenReturn(Optional.of(lowStock));

        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.addOrder(order));
        verify(productRepository, never()).removeStock(anyInt(), anyInt());
    }

    // --- getAllOrders ---

    @Test
    void getAllOrders_returnsListFromRepository() {
        List<Order> orders = List.of(
                persistedOrder(1, 1, LocalDateTime.now()),
                persistedOrder(2, 2, LocalDateTime.now())
        );
        when(orderRepository.getAllOrders()).thenReturn(orders);

        List<Order> result = orderUseCaseImpl.getAllOrders();

        assertEquals(2, result.size());
        verify(orderRepository, times(1)).getAllOrders();
    }

    @Test
    void getAllOrders_emptyRepository_returnsEmptyList() {
        when(orderRepository.getAllOrders()).thenReturn(List.of());

        List<Order> result = orderUseCaseImpl.getAllOrders();

        assertTrue(result.isEmpty());
    }

    // --- getOrdersByUserId ---

    @Test
    void getOrdersByUserId_userDoesNotExist_throwsUserNotFoundException() {
        when(userRepository.getUserById(99)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> orderUseCaseImpl.getOrdersByUserId(99));
        verify(orderRepository, never()).getOrdersByUserId(99);
    }

    @Test
    void getOrdersByUserId_returnsMatchingOrdersFromRepository() {
        when(userRepository.getUserById(5)).thenReturn(Optional.of(existingUser(5)));
        List<Order> orders = List.of(
                persistedOrder(1, 5, LocalDateTime.now()),
                persistedOrder(2, 5, LocalDateTime.now())
        );
        when(orderRepository.getOrdersByUserId(5)).thenReturn(orders);

        List<Order> result = orderUseCaseImpl.getOrdersByUserId(5);

        assertEquals(2, result.size());
        verify(orderRepository, times(1)).getOrdersByUserId(5);
    }

    @Test
    void getOrdersByUserId_userHasNoOrders_returnsEmptyList() {
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existingUser(1)));
        when(orderRepository.getOrdersByUserId(1)).thenReturn(List.of());

        List<Order> result = orderUseCaseImpl.getOrdersByUserId(1);

        assertTrue(result.isEmpty());
    }

    // --- getOrdersByProductId ---

    @Test
    void getOrdersByProductId_productDoesNotExist_throwsProductNotFoundException() {
        when(productRepository.getProductById(99)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> orderUseCaseImpl.getOrdersByProductId(99));
        verify(orderRepository, never()).getOrdersByProductId(99);
    }

    @Test
    void getOrdersByProductId_returnsMatchingOrdersFromRepository() {
        when(productRepository.getProductById(10)).thenReturn(Optional.of(existingProduct(10)));
        List<Order> orders = List.of(persistedOrder(1, 1, LocalDateTime.now()));
        when(orderRepository.getOrdersByProductId(10)).thenReturn(orders);

        List<Order> result = orderUseCaseImpl.getOrdersByProductId(10);

        assertEquals(1, result.size());
        verify(orderRepository, times(1)).getOrdersByProductId(10);
    }

    @Test
    void getOrdersByProductId_noOrdersContainProduct_returnsEmptyList() {
        when(productRepository.getProductById(1)).thenReturn(Optional.of(existingProduct(1)));
        when(orderRepository.getOrdersByProductId(1)).thenReturn(List.of());

        List<Order> result = orderUseCaseImpl.getOrdersByProductId(1);

        assertTrue(result.isEmpty());
    }

    // --- cancelOrder ---

    @Test
    void cancelOrder_orderDoesNotExist_throwsOrderNotFoundException_andNeverPersists() {
        when(orderRepository.getOrderById(99)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderUseCaseImpl.cancelOrder(99));
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    void cancelOrder_alreadyCancelledOrder_throwsOrderNotValidException_andNeverPersists() {
        Order cancelled = new Order(1, 1, List.of(validLine()),
                LocalDateTime.now().minusHours(2), LocalDate.now().plusDays(3),
                new BigDecimal("999.99"), OrderState.CANCELLED);
        when(orderRepository.getOrderById(1)).thenReturn(Optional.of(cancelled));

        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.cancelOrder(1));
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    void cancelOrder_outsideCancellationWindow_throwsOrderNotValidException_andNeverPersists() {
        Order order = new Order(1, 1, List.of(validLine()),
                LocalDateTime.now().minusHours(2), LocalDate.now().plusDays(3),
                new BigDecimal("999.99"), OrderState.CONFIRMED);
        when(orderRepository.getOrderById(1)).thenReturn(Optional.of(order));

        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.cancelOrder(1));
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    void cancelOrder_withinCancellationWindow_setsStateToCancelled() {
        Order order = new Order(1, 1, List.of(validLine()),
                LocalDateTime.now(), LocalDate.now().plusDays(3),
                new BigDecimal("999.99"), OrderState.CONFIRMED);
        when(orderRepository.getOrderById(1)).thenReturn(Optional.of(order));
        when(orderRepository.addOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        orderUseCaseImpl.cancelOrder(1);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).addOrder(captor.capture());
        assertEquals(OrderState.CANCELLED, captor.getValue().getOrderState());
    }

    @Test
    void cancelOrder_withinCancellationWindow_doesNotPhysicallyDeleteOrder() {
        Order order = new Order(1, 1, List.of(validLine()),
                LocalDateTime.now(), LocalDate.now().plusDays(3),
                new BigDecimal("999.99"), OrderState.CONFIRMED);
        when(orderRepository.getOrderById(1)).thenReturn(Optional.of(order));
        when(orderRepository.addOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        orderUseCaseImpl.cancelOrder(1);

        verify(orderRepository, times(1)).addOrder(any());
    }

    @Test
    void cancelOrder_withinCancellationWindow_callsAddStockForEachLine() {
        OrderLine line1 = line(1, "Laptop", new BigDecimal("999.99"), 2);
        OrderLine line2 = line(2, "Mouse", new BigDecimal("29.99"), 1);
        Order order = new Order(1, 1, List.of(line1, line2),
                LocalDateTime.now(), LocalDate.now().plusDays(3),
                new BigDecimal("2029.97"), OrderState.CONFIRMED);
        when(orderRepository.getOrderById(1)).thenReturn(Optional.of(order));
        when(orderRepository.addOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        orderUseCaseImpl.cancelOrder(1);

        verify(productRepository).addStock(1, 2);
        verify(productRepository).addStock(2, 1);
    }

    @Test
    void cancelOrder_alreadyCancelled_neverCallsAddStock() {
        Order cancelled = new Order(1, 1, List.of(validLine()),
                LocalDateTime.now().minusHours(2), LocalDate.now().plusDays(3),
                new BigDecimal("999.99"), OrderState.CANCELLED);
        when(orderRepository.getOrderById(1)).thenReturn(Optional.of(cancelled));

        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.cancelOrder(1));
        verify(productRepository, never()).addStock(anyInt(), anyInt());
    }

    @Test
    void cancelOrder_outsideCancellationWindow_neverCallsAddStock() {
        Order order = new Order(1, 1, List.of(validLine()),
                LocalDateTime.now().minusHours(2), LocalDate.now().plusDays(3),
                new BigDecimal("999.99"), OrderState.CONFIRMED);
        when(orderRepository.getOrderById(1)).thenReturn(Optional.of(order));

        assertThrows(OrderNotValidException.class, () -> orderUseCaseImpl.cancelOrder(1));
        verify(productRepository, never()).addStock(anyInt(), anyInt());
    }
}
