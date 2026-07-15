package com.project.e_commerce.domain.ports.in;

import com.project.e_commerce.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductUseCase {
    Product addProduct(Product product);
    Page<Product> getAllProducts(Pageable pageable);
    Product getProductById(int productId);
    Product updateProduct(Product product);
    void removeProduct(int productId);
}
