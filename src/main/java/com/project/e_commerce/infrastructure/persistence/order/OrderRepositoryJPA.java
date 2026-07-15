package com.project.e_commerce.infrastructure.persistence.order;


import com.project.e_commerce.domain.Order;
import com.project.e_commerce.domain.ports.out.OrderRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryJPA implements OrderRepository {
    private final OrderJPARepository orderJPARepository;

    public OrderRepositoryJPA(OrderJPARepository orderJPARepository) {
        this.orderJPARepository = orderJPARepository;
    }

    @Override
    public Order addOrder(Order order) {
        return OrderMapper.toDomain(orderJPARepository.save(OrderMapper.toEntity(order)));
    }

    @Override
    public List<Order> getAllOrders() {
        return OrderMapper.toDomainList(orderJPARepository.findAll());
    }

    @Override
    public Optional<Order> getOrderById(int orderId) {
        Optional<OrderEntity> orderEntityOptional = orderJPARepository.findById(orderId);
        return orderEntityOptional.map(OrderMapper::toDomain);
    }

    @Override
    public List<Order> getOrdersByUserId(int userId) {
        return OrderMapper.toDomainList(orderJPARepository.findByUserId(userId));
    }

    @Override
    public List<Order> getOrdersByProductId(int productId) {
        return OrderMapper.toDomainList(orderJPARepository.findOrderByProductId(productId));
    }

}
