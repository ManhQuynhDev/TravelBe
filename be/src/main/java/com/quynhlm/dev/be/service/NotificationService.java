package com.quynhlm.dev.be.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.model.entity.Notification;
import com.quynhlm.dev.be.repositories.NotificationRepository;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public Notification saveNotification(Integer userId,String title, String message) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setNotificationTime(LocalDateTime.now().toString());
        return notificationRepository.save(notification);
    }
}
