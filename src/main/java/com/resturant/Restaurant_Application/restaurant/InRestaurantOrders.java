package com.resturant.Restaurant_Application.restaurant;

import com.resturant.Restaurant_Application.customer.entity.Enum.Status;
import com.resturant.Restaurant_Application.customer.entity.PaymentEntity;
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
@Entity
@Builder
@Table(name = "in_restaurant_orders")
public class InRestaurantOrders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "order_date")
    @CreationTimestamp
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    @Builder.Default
    private Status orderStatus = Status.PENDING;

    @Column(precision = 10, scale = 2, nullable = false, name = "price")
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    // Many-to-Many relationship with Toppings
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "order_toppings",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "topping_id")
    )
    @Builder.Default
    private List<Toppings> toppings = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private InRestaurantPayments payment;
}
