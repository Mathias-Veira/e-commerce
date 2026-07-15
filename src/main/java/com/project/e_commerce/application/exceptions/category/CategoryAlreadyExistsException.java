package com.project.e_commerce.application.exceptions.category;

public class CategoryAlreadyExistsException extends RuntimeException{
    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
}
