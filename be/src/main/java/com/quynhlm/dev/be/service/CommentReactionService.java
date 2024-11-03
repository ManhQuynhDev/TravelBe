package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import com.quynhlm.dev.be.core.exception.CommentNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.entity.Comment;
import com.quynhlm.dev.be.model.entity.CommentReaction;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.CommentReactionRepository;
import com.quynhlm.dev.be.repositories.CommentRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentReactionService {
    @Autowired
    private CommentReactionRepository commentReactionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    public void updateReaction(CommentReaction commentReaction)
            throws CommentNotFoundException, UserAccountNotFoundException, UnknownException {

        Comment foundComment = commentRepository.findComment(commentReaction.getCommentId());

        if (foundComment == null) {
            throw new CommentNotFoundException(
                    "Comment find with id " + commentReaction.getCommentId() + " not found. Please try another!");
        }

        User foundUser = userRepository.getAnUser(commentReaction.getUserId());

        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User find with id " + commentReaction.getUserId() + " not found. Please try another!");
        }

        CommentReaction foundReaction = commentReactionRepository.findByCommentIdAndUserId(
                commentReaction.getCommentId(),
                commentReaction.getUserId());
        if (foundReaction != null) {
            if (foundReaction.getType() == commentReaction.getType()) {
                commentReactionRepository.delete(foundReaction);
            } else {
                foundReaction.setType(commentReaction.getType());
                isSuccess(foundReaction);
            }
        } else {
            CommentReaction newReaction = new CommentReaction();
            newReaction.setType(commentReaction.getType());
            newReaction.setCommentId(commentReaction.getCommentId());
            newReaction.setUserId(commentReaction.getUserId());
            newReaction.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            isSuccess(newReaction);
        }
    }

    public void isSuccess(CommentReaction commentReaction) {
        CommentReaction saveReaction = commentReactionRepository.save(commentReaction);
        if (saveReaction.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }
}
