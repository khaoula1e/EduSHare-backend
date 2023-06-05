package com.bezkoder.spring.login.security.services;


import org.springframework.stereotype.Service;

import com.bezkoder.spring.login.models.Notification;
import com.bezkoder.spring.login.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Notification getNotificationById(String id) {
        return notificationRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));
    }

    public void markAsRead(String id) {
        Notification notification = getNotificationById(id);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public Notification createNotification(Notification notification) {
        notification.setTimestamp(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationByReceiver(String id) {
        return notificationRepository.findByReceiver(id);
    }
}
