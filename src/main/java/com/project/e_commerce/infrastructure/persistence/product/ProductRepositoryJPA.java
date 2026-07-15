package com.project.e_commerce.infrastructure.persistence.product;

import com.project.e_commerce.domain.Product;
import com.project.e_commerce.domain.ports.out.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.util.Optional;
@Repository
public class ProductRepositoryJPA implements ProductRepository {
    private final ProductJPARepository productJPARepository;

    public ProductRepositoryJPA(ProductJPARepository productJPARepository) {
        this.productJPARepository = productJPARepository;
    }

    @Override
    public Product addProduct(Product product) {
        return ProductMapper.toDomain(productJPARepository.save(ProductMapper.toEntity(product)));
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return ProductMapper.toDomainPage(productJPARepository.findAll(pageable));
    }

    @Override
    public Optional<Product> getProductById(int productId) {
        Optional<ProductEntity> productEntityOptional = productJPARepository.findById(productId);
        return productEntityOptional.map(ProductMapper::toDomain);
    }

    @Override
    public boolean productAlreadyExists(String productName, BigDecimal productPrice, String productDescription) {
        Optional<ProductEntity> productEntityOptional = productJPARepository.findExactProduct(productName,productPrice,productDescription);
        return productEntityOptional.isPresent();
    }

    @Override
    public void removeStock(int productId, int quantity) {
        Optional<Product> productOptional = getProductById(productId);
        Product product = productOptional.get();
        product.setProductStock(product.getProductStock() - quantity);
        productJPARepository.save(ProductMapper.toEntity(product));
    }

    @Override
    public void addStock(int productId, int quantity) {
        Product product = getProductById(productId).get();
        product.setProductStock(product.getProductStock() + quantity);
        productJPARepository.save(ProductMapper.toEntity(product));
    }

}
