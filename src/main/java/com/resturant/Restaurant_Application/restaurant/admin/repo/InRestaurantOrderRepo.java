package com.resturant.Restaurant_Application.restaurant.admin.repo;

import com.resturant.Restaurant_Application.customer.entity.Enum.Status;
import com.resturant.Restaurant_Application.restaurant.InRestaurantOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InRestaurantOrderRepo extends JpaRepository<InRestaurantOrders, Integer> {

    /* ---------------- BASIC QUERIES ---------------- */

    List<InRestaurantOrders> findByOrderStatus(Status status);

    List<InRestaurantOrders> findByPriceBetween(
            BigDecimal min,
            BigDecimal max
    );

    List<InRestaurantOrders> findByOrderDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    long countByOrderDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    /* ---------------- DAILY REPORTS ---------------- */

    @Query("""
        SELECT COALESCE(SUM(o.price), 0)
        FROM InRestaurantOrders o
        WHERE o.orderDate BETWEEN :start AND :end
    """)
    BigDecimal getTotalAmountByDateRange(
            LocalDateTime start,
            LocalDateTime end
    );

    /* ---------------- WEEKLY / PERIOD REPORTS ---------------- */

    @Query("""
        SELECT COUNT(o)
        FROM InRestaurantOrders o
        WHERE o.orderDate BETWEEN :start AND :end
    """)
    long getOrderCountByDateRange(
            LocalDateTime start,
            LocalDateTime end
    );

    /* ---------------- STATUS + DATE (VERY USEFUL) ---------------- */

    List<InRestaurantOrders> findByOrderStatusAndOrderDateBetween(
            Status status,
            LocalDateTime start,
            LocalDateTime end
    );

    /* ---------------- SAFE DELETE (OPTIONAL) ---------------- */

    void deleteByIdAndOrderStatus(
            Integer id,
            Status status
    );

    @Query("SELECT o.menu.foodName, SUM(o.quantity) as totalSold, SUM(o.price) as revenue " +
            "FROM InRestaurantOrders o " +
            "GROUP BY o.menu.foodName " +
            "ORDER BY totalSold DESC")
    List<Object[]> getMenuPerformance();
}
