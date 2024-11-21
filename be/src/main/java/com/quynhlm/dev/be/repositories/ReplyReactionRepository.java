package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quynhlm.dev.be.model.entity.ReplyReaction;


public interface ReplyReactionRepository extends JpaRepository<ReplyReaction , Integer>{
    ReplyReaction findByReplyIdAndUserId(int replyId, int userId);
}
