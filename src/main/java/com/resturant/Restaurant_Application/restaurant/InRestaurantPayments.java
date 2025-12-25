package com.resturant.Restaurant_Application.restaurant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "in_restaurant_payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InRestaurantPayments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "amount_paid", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    // 🔹 Correct column name to match DB
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_restaurant_order_id", nullable = false, unique = true)
    private InRestaurantOrders order;
}
