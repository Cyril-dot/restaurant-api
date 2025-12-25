package com.resturant.Restaurant_Application.customer.entity.repo;

import com.resturant.Restaurant_Application.customer.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepo extends JpaRepository<Order, Integer> {

    // ------------------- Customer-specific Queries -------------------

    // All orders for a specific customer
    @Query(value = "SELECT * FROM order_table WHERE customer_id = :customerId", nativeQuery = true)
    List<Order> findByCustomer_Id(@Param("customerId") Integer customerId);

    // Orders by customer, ordered by date descending
    @Query(value = "SELECT * FROM order_table WHERE customer_id = :customerId ORDER BY order_date DESC", nativeQuery = true)
    List<Order> findByCustomerIdOrderByDateDesc(@Param("customerId") Integer customerId);

    // Orders by customer and status
    @Query(value = "SELECT * FROM order_table WHERE customer_id = :customerId AND order_status = :status", nativeQuery = true)
    List<Order> findByCustomerIdAndStatus(@Param("customerId") Integer customerId,
                                          @Param("status") String status);

    // Find order by ID and customer
    @Query(value = "SELECT * FROM order_table WHERE customer_id = :customerId AND id = :orderId", nativeQuery = true)
    Optional<Order> findByIdAndCustomerId(@Param("orderId") Integer orderId,
                                          @Param("customerId") Integer customerId);

    // Most recent N orders for a customer
    @Query(value = "SELECT * FROM order_table WHERE customer_id = :customerId ORDER BY order_date DESC LIMIT 5", nativeQuery = true)
    List<Order> findTop5ByCustomerIdOrderByDateDesc(@Param("customerId") Integer customerId);

    // Orders by exact date for a customer
    @Query(value = "SELECT * FROM order_table WHERE customer_id = :customerId AND DATE(order_date) = :orderDate", nativeQuery = true)
    List<Order> findByCustomerIdAndOrderDate(@Param("customerId") Integer customerId,
                                             @Param("orderDate") LocalDate orderDate);

    // Delete all orders that have no items
    @Modifying
    @Query(value = "DELETE FROM order_table WHERE id NOT IN (SELECT DISTINCT order_id FROM order_items)", nativeQuery = true)
    void deleteOrdersWithoutItems();

    // ------------------- Admin / General Queries -------------------

    // Get all orders, no customer restriction, ordered by date
    @Query(value = "SELECT * FROM order_table ORDER BY order_date DESC", nativeQuery = true)
    List<Order> findAllOrdersOrderByDateDesc();

    // Get orders by status (admin/general)
    @Query(value = "SELECT * FROM order_table WHERE order_status = :status ORDER BY order_date DESC", nativeQuery = true)
    List<Order> findAllByStatus(@Param("status") String status);

    // Get orders by exact date (admin/general)
    @Query(value = "SELECT * FROM order_table WHERE DATE(order_date) = :orderDate ORDER BY order_date DESC", nativeQuery = true)
    List<Order> findAllByOrderDate(@Param("orderDate") LocalDate orderDate);
}
