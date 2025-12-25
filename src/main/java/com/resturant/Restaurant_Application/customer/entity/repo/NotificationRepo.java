package com.resturant.Restaurant_Application.customer.entity.repo;

import com.resturant.Restaurant_Application.customer.entity.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification, Integer> {

    // Native query to fetch unread notifications
    @Query(value = "SELECT * FROM notifications WHERE is_read = false ORDER BY created_at DESC", nativeQuery = true)
    List<Notification> findUnreadNotificationsNative();

    // Native query to fetch all notifications
    @Query(value = "SELECT * FROM notifications ORDER BY created_at DESC", nativeQuery = true)
    List<Notification> findAllNotificationsNative();

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id")
    void markAsRead(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.read = false")
    void markAllAsRead();


}
