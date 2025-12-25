package com.resturant.Restaurant_Application.customer.security;

import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.restaurant.admin.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRefreshTokenRepo
        extends JpaRepository<AdminRefreshToken, Long> {

    Optional<AdminRefreshToken> findByToken(String token);

    Optional<AdminRefreshToken> findByAdmin(AdminEntity admin);

    void deleteByAdmin(AdminEntity admin);
}

