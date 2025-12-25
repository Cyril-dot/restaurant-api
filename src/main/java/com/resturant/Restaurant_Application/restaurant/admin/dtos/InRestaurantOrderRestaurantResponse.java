package com.resturant.Restaurant_Application.restaurant.admin.dtos;

import com.resturant.Restaurant_Application.customer.entity.Enum.Status;
import com.resturant.Restaurant_Application.customer.entity.dtos.MenuResponse;
import com.resturant.Restaurant_Application.customer.entity.dtos.ToppingsResponse;
import com.resturant.Restaurant_Application.restaurant.Menu;
import com.resturant.Restaurant_Application.restaurant.Toppings;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InRestaurantOrderRestaurantResponse {

    private Integer id;
    private LocalDateTime orderDate;

    private Status orderStatus;
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;
    private MenuResponse menu;
    private List<ToppingsResponse> toppings = new ArrayList<>();
}
