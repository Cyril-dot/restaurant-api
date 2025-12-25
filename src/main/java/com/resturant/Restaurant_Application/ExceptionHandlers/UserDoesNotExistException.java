package com.resturant.Restaurant_Application.ExceptionHandlers;

public class UserDoesNotExistException extends RuntimeException{
    private String message;

    public UserDoesNotExistException(){}

    public UserDoesNotExistException(String message){
        super(message);
        this.message = message;
    }

}
