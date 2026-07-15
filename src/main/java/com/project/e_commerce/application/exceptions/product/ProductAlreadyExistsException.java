package com.project.e_commerce.application.exceptions.product;

public class ProductAlreadyExistsException extends RuntimeException{
    public ProductAlreadyExistsException(String message) {
        super(message);
    }
}
