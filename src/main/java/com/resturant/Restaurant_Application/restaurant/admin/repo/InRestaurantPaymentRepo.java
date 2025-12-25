package com.resturant.Restaurant_Application.restaurant.admin.repo;

import com.resturant.Restaurant_Application.restaurant.InRestaurantPayments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InRestaurantPaymentRepo extends JpaRepository<InRestaurantPayments, Integer> {
}
