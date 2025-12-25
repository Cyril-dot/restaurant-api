package com.resturant.Restaurant_Application.ExceptionHandlers;

public class AttendanceNotFoundException extends RuntimeException {
    public AttendanceNotFoundException(String message) { super(message); }
}
