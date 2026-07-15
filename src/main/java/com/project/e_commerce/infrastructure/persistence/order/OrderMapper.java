package com.project.e_commerce.infrastructure.persistence.order;

import com.project.e_commerce.domain.Order;
import com.project.e_commerce.domain.OrderLine;

import java.util.List;

public class OrderMapper {

    public static Order toDomain(OrderEntity entity) {
        if (entity == null) return null;
        return new Order(
                entity.getOrderId(),
                entity.getUserId(),
                entity.getItems().stream().map(OrderMapper::orderLineToDomain).toList(),
                entity.getOrderDate(),
                entity.getDeliveryDate(),
                entity.getOrderTotal(),
                entity.getOrderState()
        );
    }

    public static Order toDomainShallow(OrderEntity entity) {
        if (entity == null) return null;
        return new Order(
                entity.getOrderId(),
                entity.getUserId(),
                List.of(),
                entity.getOrderDate(),
                entity.getDeliveryDate(),
                entity.getOrderTotal(),
                entity.getOrderState()
        );
    }

    public static OrderEntity toEntity(Order order) {
        if (order == null) return null;
        OrderEntity entity = new OrderEntity(
                order.getOrderId(),
                order.getUserId(),
                List.of(),
                order.getOrderDate(),
                order.getDeliveryDate(),
                order.getOrderTotal(),
                order.getOrderState()
        );
        entity.setItems(order.getItems().stream()
                .map(line -> orderLineToEntity(line, entity))
                .toList());
        return entity;
    }

    public static OrderEntity toEntityShallow(Order order) {
        if (order == null) return null;
        return new OrderEntity(
                order.getOrderId(),
                order.getUserId(),
                List.of(),
                order.getOrderDate(),
                order.getDeliveryDate(),
                order.getOrderTotal(),
                order.getOrderState()
        );
    }

    public static List<Order> toDomainList(List<OrderEntity> entities) {
        return entities.stream().map(OrderMapper::toDomain).toList();
    }

    public static List<Order> toDomainShallowList(List<OrderEntity> entities) {
        return entities.stream().map(OrderMapper::toDomainShallow).toList();
    }

    public static List<OrderEntity> toEntityList(List<Order> orders) {
        return orders.stream().map(OrderMapper::toEntity).toList();
    }

    private static OrderLine orderLineToDomain(OrderLineEntity entity) {
        return new OrderLine(
                entity.getProductId(),
                entity.getProductName(),
                entity.getProductUnitPrice(),
                entity.getQuantity()
        );
    }

    private static OrderLineEntity orderLineToEntity(OrderLine line, OrderEntity parent) {
        return new OrderLineEntity(
                0,
                parent,
                line.getProductId(),
                line.getProductName(),
                line.getProductUnitPrice(),
                line.getQuantity()
        );
    }
}
