package com.resturant.Restaurant_Application.customer.entity.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private String paymentMethod; // CASH, CARD, etc.
    private BigDecimal amountPaid; // amount sent by user
}


