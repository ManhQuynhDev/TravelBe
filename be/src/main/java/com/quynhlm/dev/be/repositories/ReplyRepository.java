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

    @Query(value = """
                select
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
            where comment_id = :comment_id
                    """, nativeQuery = true)
    List<Object[]> fetchReplyByCommentId(@Param("comment_id") Integer comment_id);

}
