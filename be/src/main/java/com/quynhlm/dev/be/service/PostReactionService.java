package com.quynhlm.dev.be.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.PostNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.responseDTO.UserReactionDTO;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.PostReaction;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.PostReactionRepository;
import com.quynhlm.dev.be.repositories.PostRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostReactionService {

    @Autowired
    private PostReactionRepository postReactionRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public void addReaction(PostReaction postReaction)
            throws PostNotFoundException, UserAccountNotFoundException, UnknownException {

        Post foundPost = postRepository.getAnPost(postReaction.getPostId());

        if (foundPost == null) {
            throw new PostNotFoundException(
                    "Post find with id " + postReaction.getPostId() + " not found. Please try another!");
        }

        User foundUser = userRepository.getAnUser(postReaction.getUserId());

        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User find with id " + postReaction.getUserId() + " not found. Please try another!");
        }
        postReaction.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
        isSuccess(postReaction);
    }

    public void updateReaction(PostReaction postReaction)
            throws PostNotFoundException, UserAccountNotFoundException, UnknownException {

        Post foundPost = postRepository.getAnPost(postReaction.getPostId());

        if (foundPost == null) {
            throw new PostNotFoundException(
                    "Post find with id " + postReaction.getPostId() + " not found. Please try another!");
        }

        User foundUser = userRepository.getAnUser(postReaction.getUserId());

        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User find with id " + postReaction.getUserId() + " not found. Please try another!");
        }

        PostReaction foundReaction = postReactionRepository.getAnReactionWithUserIdAndPostId(postReaction.getPostId(),
                postReaction.getUserId());

        if (foundReaction != null) {
            if (foundReaction.getType().equals(postReaction.getType())) {
                postReactionRepository.delete(foundReaction);
            } else {
                foundReaction.setType(postReaction.getType());
                isSuccess(foundReaction);
            }
        } else {
            PostReaction newReaction = new PostReaction();
            newReaction.setType(postReaction.getType());
            newReaction.setPostId(postReaction.getPostId());
            newReaction.setUserId(postReaction.getUserId());
            newReaction.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            isSuccess(newReaction);
        }
    }

    public void isSuccess(PostReaction postReaction) {
        PostReaction saveReaction = postReactionRepository.save(postReaction);
        if (saveReaction.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    // Get Reaction with type
    public Page<UserReactionDTO> getAllUserReactionWithType(String type, Pageable pageable) {
        Page<Object[]> results = postReactionRepository.getUserReactionByType(pageable, type);

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
}
