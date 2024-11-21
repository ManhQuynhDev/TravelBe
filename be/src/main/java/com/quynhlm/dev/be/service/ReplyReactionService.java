package com.quynhlm.dev.be.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.ReplyNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.entity.Reply;
import com.quynhlm.dev.be.model.entity.ReplyReaction;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.ReplyReactionRepository;
import com.quynhlm.dev.be.repositories.ReplyRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReplyReactionService {
    @Autowired
    private ReplyReactionRepository replyReactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReplyRepository replyRepository;

    public void updateReaction(ReplyReaction replyReaction)
            throws ReplyNotFoundException, UserAccountNotFoundException, UnknownException {

        Reply foundReply = replyRepository.findReply(replyReaction.getReplyId());

        if (foundReply == null) {
            throw new ReplyNotFoundException(
                    "Reply find with id " + replyReaction.getReplyId() + " not found. Please try another!");
        }

        User foundUser = userRepository.getAnUser(replyReaction.getUserId());

        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User find with id " + replyReaction.getUserId() + " not found. Please try another!");
        }

        ReplyReaction foundReaction = replyReactionRepository.findByReplyIdAndUserId(
                replyReaction.getReplyId(),
                replyReaction.getUserId());
        if (foundReaction != null) {
            if (foundReaction.getType() == replyReaction.getType()) {
                replyReactionRepository.delete(foundReaction);
            } else {
                foundReaction.setType(replyReaction.getType());
                isSuccess(foundReaction);
            }
        } else {
            ReplyReaction newReaction = new ReplyReaction();
            newReaction.setType(replyReaction.getType());
            newReaction.setReplyId(replyReaction.getReplyId());
            newReaction.setUserId(replyReaction.getUserId());
            newReaction.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            isSuccess(newReaction);
        }
    }

    public void isSuccess(ReplyReaction replyReaction) throws UnknownException {
        ReplyReaction saveReaction = replyReactionRepository.save(replyReaction);
        if (saveReaction.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }
}
