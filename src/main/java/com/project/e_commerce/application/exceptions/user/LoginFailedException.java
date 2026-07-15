package com.project.e_commerce.application.exceptions.user;

public class LoginFailedException extends RuntimeException{
    public LoginFailedException(String message) {
        super(message);
    }
}
