package com.project.e_commerce.domain.ports.out;

import com.project.e_commerce.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository {
    Product addProduct(Product product);
    Page<Product> getAllProducts(Pageable pageable);
    Optional<Product> getProductById(int productId);
    boolean productAlreadyExists(String productName, BigDecimal productPrice, String productDescription);
    void removeStock (int productId, int quantity);
    void addStock (int productId, int quantity);
}
