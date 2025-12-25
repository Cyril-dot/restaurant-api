package com.resturant.Restaurant_Application.customer.entity.Enum;

public enum Status {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    SERVED,
    COMPLETED,
    CANCELLED;

    @Override
    public String toString() {
        return this.name();
    }
}
