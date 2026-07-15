package com.project.e_commerce.infrastructure.web.controller;

import com.project.e_commerce.domain.Product;
import com.project.e_commerce.domain.ports.in.ProductUseCase;
import com.project.e_commerce.infrastructure.web.dto.request.ProductDTORequest;
import com.project.e_commerce.infrastructure.web.dto.response.ProductDTOResponse;
import com.project.e_commerce.infrastructure.web.mapper.ProductDTOMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductUseCase productUseCase;

    public ProductController(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<ProductDTOResponse> addProduct(@RequestBody ProductDTORequest request) {
        Product product = productUseCase.addProduct(ProductDTOMapper.toDomain(request));
        return ResponseEntity.ok(ProductDTOMapper.toResponse(product));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTOResponse>> getAllProducts(Pageable pageable) {
        Page<Product> products = productUseCase.getAllProducts(pageable);
        return ResponseEntity.ok(products.map(ProductDTOMapper::toResponse));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTOResponse> getProductById(@PathVariable int productId) {
        Product product = productUseCase.getProductById(productId);
        return ResponseEntity.ok(ProductDTOMapper.toResponse(product));
    }

    @PutMapping("/update")
    public ResponseEntity<ProductDTOResponse> updateProduct(@RequestBody ProductDTORequest request) {
        Product product = productUseCase.updateProduct(ProductDTOMapper.toDomain(request));
        return ResponseEntity.ok(ProductDTOMapper.toResponse(product));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeProduct(@PathVariable int productId) {
        productUseCase.removeProduct(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
