package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.model.entity.MessageStatus;
import com.quynhlm.dev.be.repositories.MessageStatusRepositoty;

@Service
public class MessageStatusService {
    @Autowired
    private MessageStatusRepositoty messageStatusRepositoty;

    public void changeStatusMessage(Integer viewId, Integer messageId, Boolean status) {
        MessageStatus foundMessage = messageStatusRepositoty.getAnMessageStatusWithUserId(viewId, messageId);

        if (foundMessage != null) {
            foundMessage.setStatus(status);

            messageStatusRepositoty.save(foundMessage);
        }
    }
}
