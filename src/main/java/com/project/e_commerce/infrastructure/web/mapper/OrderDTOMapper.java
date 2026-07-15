package com.project.e_commerce.infrastructure.web.mapper;

import com.project.e_commerce.domain.Order;
import com.project.e_commerce.domain.OrderState;
import com.project.e_commerce.infrastructure.web.dto.request.OrderDTORequest;
import com.project.e_commerce.infrastructure.web.dto.response.OrderDTOResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTOMapper {

    public static Order toDomain(OrderDTORequest request) {
        if (request == null) {
            return null;
        }
        return new Order(
                0,
                request.getUserId(),
                request.getItems(),
                LocalDateTime.now(),
                LocalDate.now(),
                BigDecimal.ZERO,
                OrderState.CONFIRMED
        );
    }

    public static OrderDTOResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderDTOResponse(
                order.getOrderId(),
                order.getUserId(),
                order.getItems(),
                order.getOrderDate(),
                order.getDeliveryDate(),
                order.getOrderTotal(),
                order.getOrderState()
        );
    }

    public static List<Order> toDomainList(List<OrderDTORequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(OrderDTOMapper::toDomain)
                .toList();
    }

    public static List<OrderDTOResponse> toResponseList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        return orders.stream()
                .map(OrderDTOMapper::toResponse)
                .toList();
    }
}
