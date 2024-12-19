package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

import com.quynhlm.dev.be.core.exception.CommentNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.responseDTO.ReactionStatisticsDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserReactionDTO;
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

    public Page<UserReactionDTO> getAllUserReactionWithType(Integer comment_id , String type, Pageable pageable) {
        Page<Object[]> results = commentReactionRepository.getUserReactionByType(pageable, type , comment_id);

        return results.map(row -> {
            UserReactionDTO userReactionDTO = new UserReactionDTO();
            userReactionDTO.setOwnerId(((Number) row[0]).intValue());
            userReactionDTO.setFullname((String) row[1]);
            userReactionDTO.setAvatar((String) row[2]);
            userReactionDTO.setType((String) row[3]);
            userReactionDTO.setCreate_time((String) row[4]);
            return userReactionDTO;
        });
    }

    public ReactionStatisticsDTO getReactionTypeCount(Integer commnet_id) throws CommentNotFoundException {
        List<Object[]> results = commentReactionRepository.reactionTypeCount(commnet_id);

        if (results.isEmpty()) {
            throw new CommentNotFoundException(
                    "Id " + commnet_id + " not found or invalid data. Please try another!");
        }

        Object[] result = results.get(0);
        ReactionStatisticsDTO reactionStatisticsDTO = new ReactionStatisticsDTO();
        reactionStatisticsDTO.setId(((Number) result[0]).intValue());
        reactionStatisticsDTO.setLikeCount(((Number) result[1]).intValue());
        reactionStatisticsDTO.setLoveCount(((Number) result[2]).intValue());
        reactionStatisticsDTO.setHahaCount(((Number) result[3]).intValue());
        reactionStatisticsDTO.setWowCount(((Number) result[4]).intValue());
        reactionStatisticsDTO.setSadCount(((Number) result[5]).intValue());
        reactionStatisticsDTO.setAngryCount(((Number) result[6]).intValue());

        return reactionStatisticsDTO;
    }
}
