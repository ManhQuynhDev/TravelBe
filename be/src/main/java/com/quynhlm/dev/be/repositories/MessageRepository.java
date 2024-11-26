package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quynhlm.dev.be.model.entity.Message;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByGroupId(Integer groupId);

    @Query(value = "SELECT * FROM Message WHERE group_id = :group_id ORDER BY created_at ASC", nativeQuery = true)
    Page<Message> findAllMessageGroup(@Param("group_id") Integer group_id, Pageable pageable);

    @Query(value = "SELECT * FROM Message WHERE userIdReceived = :userIdReceived ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Message getAnMessageWithUser(@Param("userIdReceived") Integer userIdReceived);
}