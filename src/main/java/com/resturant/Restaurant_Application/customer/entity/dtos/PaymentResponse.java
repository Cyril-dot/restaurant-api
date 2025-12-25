package com.resturant.Restaurant_Application.customer.entity.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Integer orderId;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private String status; // SUCCESS, FAILED, etc.
    private LocalDateTime date;
}
