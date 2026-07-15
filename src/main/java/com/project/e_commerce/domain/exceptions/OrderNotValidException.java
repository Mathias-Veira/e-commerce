package com.project.e_commerce.domain.exceptions;

public class OrderNotValidException extends RuntimeException{
    public OrderNotValidException(String message) {
        super(message);
    }
}
