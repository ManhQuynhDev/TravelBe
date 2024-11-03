package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Reply;
import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    @Query(value = "SELECT * FROM reply WHERE id = :id", nativeQuery = true)
    Reply findReply(@Param("id") Integer id);

    @Query(value = "SELECT * FROM reply WHERE comment_id = :comment_id", nativeQuery = true)
    List<Reply> findReplyByCommentId(@Param("comment_id") Integer comment_id);
}
