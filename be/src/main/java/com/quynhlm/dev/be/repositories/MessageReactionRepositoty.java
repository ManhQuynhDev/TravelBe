package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quynhlm.dev.be.model.entity.MessageReaction;

public interface MessageReactionRepositoty extends JpaRepository<MessageReaction , Integer>{
        MessageReaction findByMessageIdAndUserId(int messageId, int userId);
}
