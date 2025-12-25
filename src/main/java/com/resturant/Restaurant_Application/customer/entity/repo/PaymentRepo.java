package com.resturant.Restaurant_Application.customer.entity.repo;

import com.resturant.Restaurant_Application.customer.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepo extends JpaRepository<PaymentEntity, Integer> {
    @Query(value = "SELECT p.* FROM payment_entity p " +
            "JOIN order_table o ON p.order_id = o.id " +
            "WHERE o.customer_id = :customerId", nativeQuery = true)
    List<PaymentEntity> findByCustomerId(@Param("customerId") Integer customerId);

    @Query(value = """
    SELECT * FROM payments
    WHERE DATE(payment_date) = :date
    """, nativeQuery = true)
    List<PaymentEntity> findByPaymentDate(@Param("date") LocalDate date);


    @Query(value = """
    SELECT * FROM payments
    ORDER BY payment_date DESC
    LIMIT 5
""", nativeQuery = true)
    List<PaymentEntity> findTop5MostRecentPayments();


    // to find by order id
    @Query(value = "select * from payments where order_id = :orderId", nativeQuery = true)
    PaymentEntity findByOrderId(@Param("orderId") Integer orderId);



}
