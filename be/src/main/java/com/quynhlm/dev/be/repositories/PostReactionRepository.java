package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quynhlm.dev.be.model.entity.PostReaction;

public interface PostReactionRepository extends JpaRepository<PostReaction, Integer> {
    PostReaction findByPostIdAndUserId(int postId, int userId);
}   
