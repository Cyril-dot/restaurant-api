package com.resturant.Restaurant_Application.ExceptionHandlers;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) { super(message); }
}
