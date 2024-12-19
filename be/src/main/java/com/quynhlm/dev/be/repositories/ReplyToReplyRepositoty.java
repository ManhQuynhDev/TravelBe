package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.ReplyToReply;

public interface ReplyToReplyRepositoty extends JpaRepository<ReplyToReply, Integer> {
    @Query(value = "SELECT * FROM reply_to_reply WHERE id = :id", nativeQuery = true)
    ReplyToReply findReplyResult(@Param("id") Integer id);
    @Query(value = """
            select
            DISTINCT
                r.id as id,
                m.id as reply_id,
                u.id as owner_id,
                u.fullname,
                u.avatar_url as avatar,
                r.content,
                r.create_time
            from reply_to_reply r
            inner join user u on u.id = r.user_id
            inner join reply m on m.id = r.reply_id
            where m.id = :reply_id
            GROUP BY r.id, m.id, u.id, u.fullname, u.avatar_url, r.content, r.create_time
            """, nativeQuery = true)
    List<Object[]> fetchReplyToReplyByReplyId(@Param("reply_id") Integer reply_id);
}
