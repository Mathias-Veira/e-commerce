package com.project.e_commerce.infrastructure.persistence.order;

import com.project.e_commerce.domain.OrderLine;

import java.util.List;

public class OrderLineMapper {

    public static OrderLine toDomain(OrderLineEntity entity) {
        if (entity == null) return null;
        return new OrderLine(
                entity.getProductId(),
                entity.getProductName(),
                entity.getProductUnitPrice(),
                entity.getQuantity()
        );
    }

    public static OrderLineEntity toEntity(OrderLine line, OrderEntity parent) {
        if (line == null) return null;
        return new OrderLineEntity(
                0,
                parent,
                line.getProductId(),
                line.getProductName(),
                line.getProductUnitPrice(),
                line.getQuantity()
        );
    }

    public static List<OrderLine> toDomainList(List<OrderLineEntity> entities) {
        return entities.stream().map(OrderLineMapper::toDomain).toList();
    }

    public static List<OrderLineEntity> toEntityList(List<OrderLine> lines, OrderEntity parent) {
        return lines.stream().map(line -> toEntity(line, parent)).toList();
    }
}
