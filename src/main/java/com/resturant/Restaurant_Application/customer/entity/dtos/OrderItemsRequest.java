package com.resturant.Restaurant_Application.customer.entity.dtos;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemsRequest {
    private Integer quantity;
    private String foodName;
    private List<String> toppingsName;

}
