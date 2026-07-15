package com.project.e_commerce.domain.exceptions;

public class CategoryNotValidException extends RuntimeException{
    public CategoryNotValidException(String message) {
        super(message);
    }
}
