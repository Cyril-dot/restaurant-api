package com.resturant.Restaurant_Application.restaurant.admin.repo;

import com.resturant.Restaurant_Application.restaurant.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepo extends JpaRepository<Menu, Integer> {

    // Search menu by name or category (case-insensitive)
    @Query(value = """
        SELECT *
        FROM menu
        WHERE food_name LIKE %:keyword%
           OR category LIKE %:keyword%
    """, nativeQuery = true)
    List<Menu> searchMenu(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM menu WHERE category LIKE %:dish%", nativeQuery = true)
    List<Menu> findAllByCategoryLike(@Param("dish") String category);

    // to search mainly by food item word for word
    @Query(value = "SELECT * FROM menu WHERE LOWER(food_name) = LOWER(:name)", nativeQuery = true)
    Menu findByFoodNameIgnoreCase(@Param("name") String foodName);

}
