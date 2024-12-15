package com.quynhlm.dev.be.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.entity.MessageReaction;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.MessageReactionRepositoty;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class MessageReactionService {
    @Autowired
    private MessageReactionRepositoty messageReactionRepositoty;

    @Autowired
    private UserRepository userRepository;

    public void updateReaction(MessageReaction messageReaction)
            throws UserAccountNotFoundException, UnknownException {

        User foundUser = userRepository.getAnUser(messageReaction.getUserId());

        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User find with id " + messageReaction.getUserId() + " not found. Please try another!");
        }

        MessageReaction foundReaction = messageReactionRepositoty.findByMessageIdAndUserId(
                messageReaction.getMessageId(),
                messageReaction.getUserId());
        if (foundReaction != null) {
            if (foundReaction.getType() == messageReaction.getType()) {
                messageReactionRepositoty.delete(foundReaction);
            } else {
                foundReaction.setType(messageReaction.getType());
                isSuccess(foundReaction);
            }
        } else {
            MessageReaction newReaction = new MessageReaction();
            newReaction.setType(messageReaction.getType());
            newReaction.setMessageId(messageReaction.getMessageId());
            newReaction.setUserId(messageReaction.getUserId());
            newReaction.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            isSuccess(newReaction);
        }
    }

    public void isSuccess(MessageReaction messageReaction) {
        MessageReaction saveReaction = messageReactionRepositoty.save(messageReaction);
        if (saveReaction.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }
}
