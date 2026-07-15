package com.project.e_commerce.infrastructure.error;

import com.project.e_commerce.application.exceptions.category.CategoryAlreadyExistsException;
import com.project.e_commerce.application.exceptions.category.CategoryNotFoundException;
import com.project.e_commerce.application.exceptions.order.OrderNotFoundException;
import com.project.e_commerce.application.exceptions.product.ProductAlreadyExistsException;
import com.project.e_commerce.application.exceptions.product.ProductNotFoundException;
import com.project.e_commerce.application.exceptions.user.LoginFailedException;
import com.project.e_commerce.application.exceptions.user.UserAlreadyExistsException;
import com.project.e_commerce.application.exceptions.user.UserNotFoundException;
import com.project.e_commerce.domain.exceptions.CategoryNotValidException;
import com.project.e_commerce.domain.exceptions.OrderNotValidException;
import com.project.e_commerce.domain.exceptions.ProductNotValidException;
import com.project.e_commerce.domain.exceptions.UserNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler{
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> UserAlreadyExistsException(UserAlreadyExistsException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST,exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDTO> UserNotFoundException(UserNotFoundException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.NOT_FOUND,exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(UserNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> UserNotValidException(UserNotValidException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST,exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(LoginFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> LoginFailedException(LoginFailedException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST,exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDTO> ProductNotFoundException(ProductNotFoundException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.NOT_FOUND,exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ProductNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> ProductNotValidException(ProductNotValidException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST,exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> ProductAlreadyExistsException(ProductAlreadyExistsException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST,exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDTO> CategoryNotFoundException(CategoryNotFoundException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.NOT_FOUND,exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CategoryNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> CategoryNotValidException(CategoryNotValidException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST,exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> CategoryAlreadyExistsException(CategoryAlreadyExistsException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST,exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDTO> OrderNotFoundException(OrderNotFoundException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.NOT_FOUND,exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(OrderNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> OrderNotValidException(OrderNotValidException exception){
        ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST,exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
