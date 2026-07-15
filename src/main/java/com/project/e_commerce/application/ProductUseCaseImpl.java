package com.project.e_commerce.application;

import com.project.e_commerce.application.exceptions.product.ProductAlreadyExistsException;
import com.project.e_commerce.application.exceptions.product.ProductNotFoundException;
import com.project.e_commerce.domain.Category;
import com.project.e_commerce.domain.Product;
import com.project.e_commerce.domain.exceptions.ProductNotValidException;
import com.project.e_commerce.domain.ports.in.ProductUseCase;
import com.project.e_commerce.domain.ports.out.CategoryRepository;
import com.project.e_commerce.domain.ports.out.ProductRepository;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductUseCaseImpl implements ProductUseCase {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductUseCaseImpl(ProductRepository productRepository,CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Product addProduct(Product product) {
        if(!Product.validatePrice(product.getProductPrice())|| !Product.validateStock(product.getProductStock())|| !Product.validateDescription(product.getProductDescription()) || !Product.validateCategories(product.getCategories())){
            throw new ProductNotValidException("This product is not valid");

        }
        if(productRepository.productAlreadyExists(product.getProductName(), product.getProductPrice(), product.getProductDescription())) throw new ProductAlreadyExistsException("This product already exists");
        List<Integer> categoriesId = product.getCategories().stream()
                .map(Category::getCategoryId).collect(Collectors.toList());

        if(categoryRepository.getCategoriesById(categoriesId).size()< categoriesId.size()) throw new ProductNotValidException("The product has to be in a category");
        return productRepository.addProduct(product);
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.getAllProducts(pageable);
    }

    @Override
    public Product getProductById(int productId) {
        Optional<Product> productOptional = productRepository.getProductById(productId);
        if(productOptional.isEmpty()) throw new ProductNotFoundException("This product does not exist");
        return productOptional.get();
    }

    @Override
    public Product updateProduct(Product product) {
        Optional<Product> productOptional = productRepository.getProductById(product.getProductId());
        if(productOptional.isEmpty()) throw new ProductNotFoundException("This product does not exist");
        if(!Product.validatePrice(product.getProductPrice())|| !Product.validateStock(product.getProductStock())|| !Product.validateDescription(product.getProductDescription()) || !Product.validateCategories(product.getCategories()) || product.isDiscontinued()){
            throw new ProductNotValidException("This product is not valid to update");

        }
        Product updatedProduct = productOptional.get();
        updatedProduct.setProductName(product.getProductName());
        updatedProduct.setCategories(product.getCategories());
        updatedProduct.setProductPrice(product.getProductPrice());
        updatedProduct.setProductDescription(product.getProductDescription());
        updatedProduct.setProductStock(product.getProductStock());
        return productRepository.addProduct(updatedProduct);
    }

    @Override
    public void removeProduct(int productId) {
        Optional<Product> productOptional = productRepository.getProductById(productId);
        if(productOptional.isEmpty()) throw new ProductNotFoundException("This product does not exist");
        Product product = productOptional.get();
        product.setDiscontinued(true);
        productRepository.addProduct(product);
    }

}
