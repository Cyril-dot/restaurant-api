package com.resturant.Restaurant_Application.customer.entity.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuResponse {
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
}
