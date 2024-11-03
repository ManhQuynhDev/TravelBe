package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quynhlm.dev.be.model.entity.CommentReaction;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Integer> {
    CommentReaction findByCommentIdAndUserId(int commentId, int userId);
}   
