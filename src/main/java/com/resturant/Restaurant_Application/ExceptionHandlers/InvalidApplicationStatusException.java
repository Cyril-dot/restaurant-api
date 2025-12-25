package com.resturant.Restaurant_Application.ExceptionHandlers;

public class InvalidApplicationStatusException extends RuntimeException {
    public InvalidApplicationStatusException(String message) {
        super(message);
    }
}