package com.resturant.Restaurant_Application.ExceptionHandlers;

public class InvalidDepartmentFacultyException extends RuntimeException {
    public InvalidDepartmentFacultyException(String message) {
        super(message);
    }

    public InvalidDepartmentFacultyException(String message, Throwable cause) {
        super(message, cause);
    }
}
