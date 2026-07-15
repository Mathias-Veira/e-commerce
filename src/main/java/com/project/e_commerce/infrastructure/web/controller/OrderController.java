package com.project.e_commerce.infrastructure.web.controller;

import com.project.e_commerce.domain.Order;
import com.project.e_commerce.domain.ports.in.OrderUseCase;
import com.project.e_commerce.infrastructure.web.dto.request.OrderDTORequest;
import com.project.e_commerce.infrastructure.web.dto.response.OrderDTOResponse;
import com.project.e_commerce.infrastructure.web.mapper.OrderDTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderUseCase orderUseCase;

    public OrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<OrderDTOResponse> addOrder(@RequestBody OrderDTORequest request) {
        Order order = orderUseCase.addOrder(OrderDTOMapper.toDomain(request));
        return ResponseEntity.ok(OrderDTOMapper.toResponse(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderDTOResponse>> getAllOrders() {
        List<Order> orders = orderUseCase.getAllOrders();
        return ResponseEntity.ok(OrderDTOMapper.toResponseList(orders));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTOResponse>> getOrdersByUserId(@PathVariable int userId) {
        List<Order> orders = orderUseCase.getOrdersByUserId(userId);
        return ResponseEntity.ok(OrderDTOMapper.toResponseList(orders));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OrderDTOResponse>> getOrdersByProductId(@PathVariable int productId) {
        List<Order> orders = orderUseCase.getOrdersByProductId(productId);
        return ResponseEntity.ok(OrderDTOMapper.toResponseList(orders));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable int orderId) {
        orderUseCase.cancelOrder(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
