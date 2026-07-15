package com.project.e_commerce.domain.exceptions;

public class UserNotValidException extends RuntimeException{
    public UserNotValidException(String message) {
        super(message);
    }
}
