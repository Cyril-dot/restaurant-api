package com.resturant.Restaurant_Application.customer.entity.dtos;

import com.resturant.Restaurant_Application.customer.entity.Enum.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompleteOrderView {
    // order details
    private LocalDateTime date;
    private Status status;
    private BigDecimal totalAmount;

    // order items
    private List<OrderItemsResponse> orderItems;
}
