package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.quynhlm.dev.be.model.entity.Notification;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.UserRepository;

@Component
public class NotificationHelper {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    public void pushNotification(Integer userReceivedId, Integer userSendId, String message, String title) {
        User foundUser = userRepository.getAnUser(userSendId);

        if (foundUser != null) {
            Notification notification = notificationService.saveNotification(
                    userReceivedId, title + " " + foundUser.getFullname(),
                    foundUser.getFullname() + " " + message);

            messagingTemplate.convertAndSend("/topic/notification/" + userReceivedId, notification);
        }
    }
}