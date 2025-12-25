package com.resturant.Restaurant_Application.restaurant.admin.dtos;

import com.resturant.Restaurant_Application.customer.entity.Enum.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InRestaurantPaymentResponse {
    private String paymentMethod; // CASH, CARD, etc.
    private BigDecimal amountPaid;
    private LocalDateTime paymentDate;

    private Integer orderId;
    private LocalDateTime orderDate;
    private Status orderStatus;
}
