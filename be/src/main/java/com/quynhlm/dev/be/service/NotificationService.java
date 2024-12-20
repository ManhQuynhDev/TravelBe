package com.quynhlm.dev.be.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.NotificationNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.entity.Notification;
import com.quynhlm.dev.be.repositories.NotificationRepository;
import com.quynhlm.dev.be.repositories.NotificationResponseDTO;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public List<NotificationResponseDTO> getAllLotificationWithId(Integer user_id) {
        List<Object[]> results = notificationRepository.getAllNotificationWithUserId(user_id);

        List<NotificationResponseDTO> notifications = new ArrayList<>();

        for (Object[] row : results) {
            NotificationResponseDTO notification = new NotificationResponseDTO();

            notification.setId(((Number) row[0]).intValue());
            notification.setTitle((String) row[1]);
            notification.setMessage((String) row[2]);
            notification.setMediaUrl((String) row[3]);
            notification.setStatus((String) row[4]);
            notification.setUserId(((Number) row[5]).intValue());
            notification.setFullname((String) row[6]);
            notification.setAvatarUrl((String) row[7]);
            notification.setNotificationTime((String) row[8]);

            notifications.add(notification);
        }
        return notifications;
    }

    public NotificationResponseDTO getAnLotificationWithId(Integer id) {
        List<Object[]> results = notificationRepository.getAllNotificationWithId(id);

        Object[] row = results.get(0);

        NotificationResponseDTO notification = new NotificationResponseDTO();

        notification.setId(((Number) row[0]).intValue());
        notification.setTitle((String) row[1]);
        notification.setMessage((String) row[2]);
        notification.setMediaUrl((String) row[3]);
        notification.setStatus((String) row[4]);
        notification.setUserId(((Number) row[5]).intValue());
        notification.setFullname((String) row[6]);
        notification.setAvatarUrl((String) row[7]);
        notification.setNotificationTime((String) row[8]);

        return notification;
    }

    public NotificationResponseDTO saveNotification(Notification notification) throws UnknownException {
        notification.setNotificationTime(LocalDateTime.now().toString());

        Notification saveNotification = notificationRepository.save(notification);
        if (saveNotification.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
        return getAnLotificationWithId(saveNotification.getId());
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
