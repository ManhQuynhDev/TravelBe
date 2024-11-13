package com.quynhlm.dev.be.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.StoryNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.entity.Story;
import com.quynhlm.dev.be.model.entity.StoryReaction;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.StoryReactionRepository;
import com.quynhlm.dev.be.repositories.StoryRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class StoryReactionService {
    @Autowired
    private StoryReactionRepository storyReactionRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    public void addReaction(StoryReaction storyReaction)
            throws StoryNotFoundException, UserAccountNotFoundException {

        Story foundStory = storyRepository.getAnStory(storyReaction.getStoryId());

        if (foundStory == null) {
            throw new StoryNotFoundException(
                    "Story find with id " + storyReaction.getStoryId() + " not found. Please try another!");
        }

        User foundUser = userRepository.getAnUser(storyReaction.getUserId());

        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User find with id " + storyReaction.getUserId() + " not found. Please try another!");
        }
        storyReaction.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
        isSuccess(storyReaction);
    }

    public void updateReaction(StoryReaction storyReaction)
            throws StoryNotFoundException, UserAccountNotFoundException {

        Story foundStory = storyRepository.getAnStory(storyReaction.getStoryId());

        if (foundStory == null) {
            throw new StoryNotFoundException(
                    "Story find with id " + storyReaction.getStoryId() + " not found. Please try another!");
        }

        User foundUser = userRepository.getAnUser(storyReaction.getUserId());

        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User find with id " + storyReaction.getUserId() + " not found. Please try another!");
        }
        StoryReaction foundReaction = storyReactionRepository.findByStoryIdAndUserId(storyReaction.getStoryId(),
                storyReaction.getUserId());
        if (foundReaction != null) {
            if (foundReaction.getType() == storyReaction.getType()) {
                storyReactionRepository.delete(foundReaction);
            } else {
                foundReaction.setType(storyReaction.getType());
                isSuccess(foundReaction);
            }
        } else {
            StoryReaction newReaction = new StoryReaction();
            newReaction.setType(storyReaction.getType());
            newReaction.setStoryId(storyReaction.getStoryId());
            newReaction.setUserId(storyReaction.getUserId());
            newReaction.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            isSuccess(newReaction);
        }
    }

    public void isSuccess(StoryReaction storyReaction) throws UnknownException {
        StoryReaction saveReaction = storyReactionRepository.save(storyReaction);
        if (saveReaction.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

}
