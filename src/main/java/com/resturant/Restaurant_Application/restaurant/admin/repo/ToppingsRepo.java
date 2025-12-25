package com.resturant.Restaurant_Application.restaurant.admin.repo;

import com.resturant.Restaurant_Application.restaurant.Toppings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ToppingsRepo extends JpaRepository<Toppings, Integer> {

    // Search toppings by name
    @Query(value = """
        SELECT *
        FROM toppings
        WHERE name LIKE %:keyword%
    """, nativeQuery = true)
    List<Toppings> searchToppings(@Param("keyword") String keyword);

    @Query(value = """
        SELECT *
        FROM toppings
        WHERE LOWER(name) = LOWER(:name)
    """, nativeQuery = true)
    Toppings findByNameIgnoreCase(@Param("name") String name);
}
