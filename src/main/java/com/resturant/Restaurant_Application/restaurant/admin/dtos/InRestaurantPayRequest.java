package com.resturant.Restaurant_Application.restaurant.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InRestaurantPayRequest {
    private String paymentMethod; // CASH, CARD, etc.
    private BigDecimal amountPaid;
}
