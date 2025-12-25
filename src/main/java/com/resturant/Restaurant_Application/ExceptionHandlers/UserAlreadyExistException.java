package com.resturant.Restaurant_Application.ExceptionHandlers;

public class UserAlreadyExistException extends RuntimeException {

    private String message;

    public UserAlreadyExistException() {}

    public UserAlreadyExistException(String message) {
        super(message);
        this.message = message;
    }

}
