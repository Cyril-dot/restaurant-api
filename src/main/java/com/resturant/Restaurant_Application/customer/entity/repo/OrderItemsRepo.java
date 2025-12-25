package com.resturant.Restaurant_Application.customer.entity.repo;

import com.resturant.Restaurant_Application.customer.entity.Order;
import com.resturant.Restaurant_Application.customer.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemsRepo extends JpaRepository<OrderItems, Integer> {

    @Query(value = "select * from order_items where order_id = :orderId", nativeQuery = true)
    List<OrderItems> findByOrderId(@Param("orderId") Integer orderId);


}
