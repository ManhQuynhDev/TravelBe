package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quynhlm.dev.be.model.entity.StoryReaction;

public interface StoryReactionRepository extends JpaRepository<StoryReaction, Integer> {
    StoryReaction findByStoryIdAndUserId(int storyId, int userId);
}
