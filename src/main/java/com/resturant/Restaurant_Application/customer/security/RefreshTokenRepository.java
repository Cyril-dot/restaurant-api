package com.resturant.Restaurant_Application.customer.security;

import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Find a refresh token by its string value
    Optional<RefreshToken> findByToken(String token);

    // Delete refresh token(s) for a given user
    Optional<RefreshToken> findByCustomer(CustomerEntity customer);
    void deleteByCustomer(CustomerEntity customer);
}
