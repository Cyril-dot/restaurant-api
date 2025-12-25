package com.resturant.Restaurant_Application.customer.controller;

import com.resturant.Restaurant_Application.customer.entity.Notification;
import com.resturant.Restaurant_Application.customer.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;

    /** ------------------ GET UNREAD NOTIFICATIONS ------------------ **/
    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications() {
        log.info("Admin requested unread notifications");
        List<Notification> notifications = notificationService.getUnreadNotifications();
        log.info("Fetched {} unread notifications", notifications.size());
        notifications.forEach(n -> log.info("Notification: {}", n));
        return notifications;
    }

    /** ------------------ GET ALL NOTIFICATIONS ------------------ **/
    @GetMapping("/all")
    public List<Notification> getAllNotifications() {
        log.info("Admin requested all notifications");
        List<Notification> notifications = notificationService.getAllNotifications();
        log.info("Fetched {} notifications", notifications.size());
        notifications.forEach(n -> log.info("Notification: {}", n));
        return notifications;
    }

    /** ------------------ MARK SINGLE NOTIFICATION AS READ ------------------ **/
    @PostMapping("/{id}/read")
    public Notification markAsRead(@PathVariable("id") Integer notificationId) {
        log.info("Admin marking notification {} as read", notificationId);
        Notification updatedNotification = notificationService.markAsRead(notificationId);
        log.info("Notification {} marked as read: {}", notificationId, updatedNotification);
        return updatedNotification;
    }

    /** ------------------ CREATE NOTIFICATION ------------------ **/
    @PostMapping("/create")
    public Notification createNotification(@RequestParam String title,
                                           @RequestParam String message) {
        log.info("Admin creating notification - Title: {}, Message: {}", title, message);
        Notification notification = notificationService.createNotification(title, message);
        log.info("Notification created with ID: {}", notification.getId());
        return notification;
    }

    /** ------------------ FULL WORKFLOW: FETCH AND MARK ALL UNREAD AS READ ------------------ **/
    @PostMapping("/mark-all-read")
    public List<Notification> markAllUnreadAsRead() {
        log.info("Admin fetching and marking all unread notifications as read");
        List<Notification> updatedNotifications = notificationService.fetchAndMarkAllUnreadAsRead();
        log.info("All unread notifications marked as read");
        return updatedNotifications;
    }
}
