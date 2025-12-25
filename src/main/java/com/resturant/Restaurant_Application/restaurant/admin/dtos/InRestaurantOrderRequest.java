package com.resturant.Restaurant_Application.restaurant.admin.dtos;

import com.resturant.Restaurant_Application.customer.entity.Enum.Status;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class InRestaurantOrderRequest {
   private String foodName;
   private List<String> toppingsName;
   private Integer quantity;
}
