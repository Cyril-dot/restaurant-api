package com.resturant.Restaurant_Application.customer.service;

import com.resturant.Restaurant_Application.customer.entity.Notification;
import com.resturant.Restaurant_Application.customer.entity.repo.NotificationRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    @PersistenceContext
    private EntityManager entityManager;
    private final NotificationRepo notificationRepo;

    /** ------------------ CREATE NOTIFICATION ------------------ **/
    public Notification createNotification(String title, String message) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now()) // Ensure created_at is set
                .build();
        return notificationRepo.save(notification);
    }

    /** ------------------ GET UNREAD NOTIFICATIONS (NATIVE) ------------------ **/
    public List<Notification> getUnreadNotifications() {
        return notificationRepo.findUnreadNotificationsNative();
    }

    /** ------------------ GET ALL NOTIFICATIONS (NATIVE) ------------------ **/
    public List<Notification> getAllNotifications() {
        return notificationRepo.findAllNotificationsNative();
    }

    /** ------------------ MARK NOTIFICATION AS READ (NATIVE) ------------------ **/
    public Notification markAsRead(Integer notificationId) {
        return notificationRepo.findById(notificationId).map(n -> {
            n.setRead(true);
            return notificationRepo.save(n); // Return the updated notification
        }).orElse(null); // Return null if notification not found
    }


    /** ------------------ FULL WORKFLOW: PRINT, MARK ALL UNREAD AS READ ------------------ **/
    @Transactional
    public List<Notification> fetchAndMarkAllUnreadAsRead() {
        List<Notification> unread = getUnreadNotifications();
        System.out.println("Unread notifications:");
        unread.forEach(System.out::println);

        // Bulk update
        notificationRepo.markAllAsRead();

        // Ensure the persistence context is cleared so we fetch fresh data
        entityManager.flush();
        entityManager.clear();

        List<Notification> updated = getAllNotifications();
        System.out.println("Notifications after marking as read:");
        updated.forEach(System.out::println);

        return updated;
    }


}
