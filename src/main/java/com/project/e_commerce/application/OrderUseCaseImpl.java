package com.project.e_commerce.application;

import com.project.e_commerce.application.exceptions.order.OrderNotFoundException;
import com.project.e_commerce.application.exceptions.product.ProductNotFoundException;
import com.project.e_commerce.application.exceptions.user.UserNotFoundException;
import com.project.e_commerce.domain.Order;
import com.project.e_commerce.domain.OrderLine;
import com.project.e_commerce.domain.OrderState;
import com.project.e_commerce.domain.Product;
import com.project.e_commerce.domain.exceptions.OrderNotValidException;
import com.project.e_commerce.domain.ports.in.OrderUseCase;
import com.project.e_commerce.domain.ports.out.OrderRepository;
import com.project.e_commerce.domain.ports.out.ProductRepository;
import com.project.e_commerce.domain.ports.out.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderUseCaseImpl implements OrderUseCase {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderUseCaseImpl(OrderRepository orderRepository,UserRepository userRepository,ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }
    @Transactional
    @Override
    public Order addOrder(Order order) {
        if(userRepository.getUserById(order.getUserId()).isEmpty()) throw new UserNotFoundException("User not found");
        List<OrderLine> orderLines = order.getItems();
        if(!Order.validateHasLines(orderLines)) throw new OrderNotValidException("Order is not valid");
        order.setItems(Order.mergeLines(orderLines));
        for (OrderLine orderLine: order.getItems()) {
            if(!OrderLine.validateQuantity(orderLine.getQuantity()) || !OrderLine.validateUnitPrice(orderLine.getProductUnitPrice())){
                throw new OrderNotValidException("Order is not valid");
            }

            Product product = productRepository.getProductById(orderLine.getProductId()).orElseThrow(()-> new ProductNotFoundException("Product not found"));
            orderLine.setProductUnitPrice(product.getProductPrice());
            orderLine.setProductName(product.getProductName());
            if(product.isDiscontinued()) throw new OrderNotValidException("Can´t create an order with a discontinued product");
            if(product.getProductStock()<orderLine.getQuantity()) throw new OrderNotValidException("Insufficient Stock");
            productRepository.removeStock(orderLine.getProductId(),orderLine.getQuantity());
        }
        order.setOrderState(OrderState.CONFIRMED);
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryDate(Order.computeDeliveryDate(order.getOrderDate()));
        order.computeTotal();
        return orderRepository.addOrder(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.getAllOrders();
    }

    @Override
    public List<Order> getOrdersByUserId(int userId) {
        if(userRepository.getUserById(userId).isEmpty()) throw new UserNotFoundException("User not found");
        return orderRepository.getOrdersByUserId(userId);
    }

    @Override
    public List<Order> getOrdersByProductId(int productId) {
        if(productRepository.getProductById(productId).isEmpty()) throw new ProductNotFoundException("Product not found");
        return orderRepository.getOrdersByProductId(productId);
    }
    @Transactional
    @Override
    public void cancelOrder(int orderId) {
        Optional<Order> orderOptional = orderRepository.getOrderById(orderId);
        if(orderOptional.isEmpty()) throw new OrderNotFoundException("Order not found");
        Order order = orderOptional.get();
        List<OrderLine> orderLines = order.getItems();
        if(order.getOrderState() == OrderState.CANCELLED) throw new OrderNotValidException("Order has already been cancelled");
        if(!order.isCancellable(LocalDateTime.now())) throw new OrderNotValidException("Time to cancel order has expired");
        order.setOrderState(OrderState.CANCELLED);
        for (OrderLine line: orderLines) {
            if(!OrderLine.validateQuantity(line.getQuantity()) || !OrderLine.validateUnitPrice(line.getProductUnitPrice())){
                throw new OrderNotValidException("Order is not valid");
            }
            productRepository.addStock(line.getProductId(), line.getQuantity());
        }
        orderRepository.addOrder(order);
    }

}
