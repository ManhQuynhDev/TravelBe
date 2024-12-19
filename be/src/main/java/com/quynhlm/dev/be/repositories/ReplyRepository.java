package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Reply;
import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    @Query(value = "SELECT * FROM reply WHERE id = :id", nativeQuery = true)
    Reply findReply(@Param("id") Integer id);

    @Query(value = """
            select
                DISTINCT
                r.id as reply_id,
                m.id as comment_id,
                u.id as owner_id,
                u.fullname,
                u.avatar_url as avatar,
                r.content,
                r.create_time
            from reply r
            inner join user u on u.id = r.user_id
            inner join comment m on m.id = r.comment_id
            LEFT JOIN reply_reaction reaction ON r.id = reaction.reply_id
            where m.id = :comment_id
            GROUP BY r.id, m.id, u.id, u.fullname, u.avatar_url, r.content, r.create_time
            """, nativeQuery = true)
    Page<Object[]> findReplyByCommentId(@Param("comment_id") Integer comment_id, Pageable pageable);

    @Query(value = """
            select
                DISTINCT
                r.id as reply_id,
                m.id as comment_id,
                u.id as owner_id,
                u.fullname,
                u.avatar_url as avatar,
                r.content,
                r.url,
                r.create_time,
                COUNT(DISTINCT reaction.id) AS reaction_count,
                MAX(CASE WHEN reaction.user_id = :userId THEN reaction.type ELSE NULL END) AS user_reaction_type
            from reply r
            inner join user u on u.id = r.user_id
            inner join comment m on m.id = r.comment_id
            LEFT JOIN reply_reaction reaction ON r.id = reaction.reply_id
            where m.id = :comment_id
            GROUP BY r.id, m.id, u.id, u.fullname, u.avatar_url, r.content, r.create_time
            """, nativeQuery = true)
    List<Object[]> fetchReplyByCommentId(@Param("comment_id") Integer comment_id , @Param("userId") Integer userId);
}
