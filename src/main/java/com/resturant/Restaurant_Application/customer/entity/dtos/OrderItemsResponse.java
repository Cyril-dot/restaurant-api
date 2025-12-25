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
public class OrderItemsResponse {
    private MenuResponse menuItem;           // single menu item per order item
    private List<ToppingsResponse> toppings; // toppings for this item
    private Integer quantity;
    private BigDecimal totalPrice;
}
