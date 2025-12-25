package com.resturant.Restaurant_Application.ExceptionHandlers;

public class InValidRoleException extends RuntimeException{
    private String message;

    public InValidRoleException() {}

    public InValidRoleException(String message) {
        super(message);
        this.message = message;
    }
}
