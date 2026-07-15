package com.project.e_commerce.infrastructure.persistence.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
@Repository
public interface ProductJPARepository extends JpaRepository<ProductEntity,Integer> {
    @Query("SELECT P FROM ProductEntity P WHERE P.productName = :product_name AND P.productPrice = :product_price AND P.productDescription = :product_description")
    Optional<ProductEntity> findExactProduct(@Param("product_name")String productName, @Param("product_price") BigDecimal productPrice, @Param("product_description")String productDescription);
}
