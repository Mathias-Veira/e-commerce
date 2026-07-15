package com.project.e_commerce.infrastructure.persistence.order;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderJPARepository extends JpaRepository<OrderEntity,Integer> {
    List<OrderEntity> findByUserId(int userId);
    @Query("SELECT O FROM OrderEntity O INNER JOIN OrderLineEntity OL ON O.orderId = OL.order.orderId  WHERE OL.productId = :product_id")
    List<OrderEntity> findOrderByProductId(@Param("product_id") int productId);

}
