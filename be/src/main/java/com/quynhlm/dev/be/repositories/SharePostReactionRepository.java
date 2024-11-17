package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quynhlm.dev.be.model.entity.SharePostReaction;

public interface SharePostReactionRepository extends JpaRepository<SharePostReaction, Integer> {
    SharePostReaction findByShareIdAndUserId(int shareId, int userId);
}
