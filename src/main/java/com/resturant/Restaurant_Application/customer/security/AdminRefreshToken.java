package com.resturant.Restaurant_Application.customer.security;

import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.restaurant.admin.AdminEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Table(name = "admin_refreshTokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class AdminRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne
    @JoinColumn(name = "admin_id", referencedColumnName = "id")
    private AdminEntity admin;

    private Instant expiryDate;

    // getters & setters
}
