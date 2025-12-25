package com.resturant.Restaurant_Application.customer.security;

import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Table(name = "refreshTokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private CustomerEntity customer;

    private Instant expiryDate;

    // getters & setters
}
