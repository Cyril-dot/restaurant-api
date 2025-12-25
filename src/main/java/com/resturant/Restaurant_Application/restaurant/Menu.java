package com.resturant.Restaurant_Application.restaurant;

import com.resturant.Restaurant_Application.customer.entity.OrderItems;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "menu")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "food_name", nullable = false)
    private String foodName;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean is_available = true;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(columnDefinition = "LONGTEXT", name = "description")
    private String description;

    @OneToMany(mappedBy = "menu")
    private List<OrderItems> orderItems = new ArrayList<>();


    @OneToMany(mappedBy = "menu")
    private List<InRestaurantOrders> inRestaurantOrders = new ArrayList<>();

}
