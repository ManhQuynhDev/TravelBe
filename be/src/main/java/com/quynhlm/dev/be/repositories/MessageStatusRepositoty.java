package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.quynhlm.dev.be.model.entity.MessageStatus;

public interface MessageStatusRepositoty extends JpaRepository<MessageStatus , Integer>{

    // @Query(value = "SELECT * FROM message_user_status WHERE user_id = :user_id", nativeQuery = true)
    // MessageStatus getAnMessageStatusWithUserId(@Param("user_id") Integer user_id);

    @Query(value = "SELECT * FROM message_status WHERE user_id = :user_id and message_id =:message_id", nativeQuery = true)
    MessageStatus getAnMessageStatusWithUserId(@Param("user_id") Integer user_id , @Param("message_id") Integer message_id);
}
