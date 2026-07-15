package com.project.e_commerce.domain.ports.out;

import com.project.e_commerce.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order addOrder(Order order);
    List<Order> getAllOrders();
    Optional<Order> getOrderById(int orderId);
    List<Order> getOrdersByUserId(int userId);
    List<Order> getOrdersByProductId(int productId);
}
