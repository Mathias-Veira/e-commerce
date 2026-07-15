package com.project.e_commerce.domain.ports.in;

import com.project.e_commerce.domain.Order;

import java.util.List;

public interface OrderUseCase {
    Order addOrder(Order order);
    List<Order> getAllOrders();
    List<Order> getOrdersByUserId(int userId);
    List<Order> getOrdersByProductId(int productId);
    void cancelOrder(int orderId);
}
