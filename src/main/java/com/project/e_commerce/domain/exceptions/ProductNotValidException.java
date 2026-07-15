package com.project.e_commerce.domain.exceptions;

public class ProductNotValidException extends RuntimeException{
    public ProductNotValidException(String message) {
        super(message);
    }
}
