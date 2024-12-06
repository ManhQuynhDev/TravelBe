package com.quynhlm.dev.be.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.NotificationNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.entity.Notification;
import com.quynhlm.dev.be.repositories.NotificationRepository;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public Notification saveNotification(Notification notification) throws UnknownException {
        notification.setNotificationTime(LocalDateTime.now().toString());

        Notification saveNotification = notificationRepository.save(notification);
        if (saveNotification.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
        return saveNotification;
    }

    public void deleteNotification(int id) throws NotificationNotFoundException {
        Notification foundNotification = notificationRepository.findNotificationById(id);
        if (foundNotification == null) {
            throw new NotificationNotFoundException("Found Notification with " + id + " not found please try again");
        }
        notificationRepository.delete(foundNotification);
    }

    public void changeStatus(int id) throws NotificationNotFoundException {
        Notification foundNotification = notificationRepository.findNotificationById(id);
        if (foundNotification == null) {
            throw new NotificationNotFoundException("Found Notification with " + id + " not found please try again");
        }

        foundNotification.setStatus(true);
        Notification saveNotification = notificationRepository.save(foundNotification);
        if (saveNotification.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }
}
