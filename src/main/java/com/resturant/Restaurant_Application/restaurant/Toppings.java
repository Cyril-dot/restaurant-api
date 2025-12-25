package com.resturant.Restaurant_Application.restaurant;

import com.resturant.Restaurant_Application.customer.entity.OrderItems;
import com.resturant.Restaurant_Application.restaurant.InRestaurantOrders;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "toppings")
public class Toppings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    // New field for availability
    @Column(name = "is_available", nullable = false)
    private Boolean is_available = true; // default to true

    // Relationship with OrderItems (Many-to-Many)
    @ManyToMany(mappedBy = "toppings")
    private List<OrderItems> orderItems = new ArrayList<>();

    // Relationship with InRestaurantOrders (Many-to-Many)
    @ManyToMany(mappedBy = "toppings", fetch = FetchType.LAZY)
    private Set<InRestaurantOrders> inRestaurantOrders = new HashSet<>();
}
