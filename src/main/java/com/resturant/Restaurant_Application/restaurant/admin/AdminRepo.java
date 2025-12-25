package com.resturant.Restaurant_Application.restaurant.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminRepo extends JpaRepository<AdminEntity, Integer> {
    Optional<AdminEntity> findByEmail(String email);
}
