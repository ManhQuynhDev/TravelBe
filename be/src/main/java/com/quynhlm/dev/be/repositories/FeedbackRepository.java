package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.quynhlm.dev.be.model.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    @Query(value = "SELECT * FROM Feedback WHERE id = :id and del_flag = 0", nativeQuery = true)
    Feedback findComment(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE Feedback f SET f.delFlag = 1 WHERE f.id = :feedbackId OR f.replyToId = :feedbackId")
    void softDeleteFeedbackAndReplies(Integer feedbackId);

    @Query(value = """
            select
            c.id AS comment_id,
                        u.id AS owner_id,
                        u.fullname,
                        u.avatar_url AS avatar,
                        c.content,
                        c.media_url AS mediaUrl,
                        c.post_id,
                        c.is_reply,
                        c.reply_to_id,
                        c.create_time
                    from feedback as c
                    INNER JOIN
                        user u ON u.id = c.user_id
                        WHERE
                        c.id = :comment_id and c.del_flag = 0;
                                """, nativeQuery = true)
    List<Object[]> findCommentWithId(@Param("comment_id") Integer comment_id);

    @Query(value = """
                 SELECT
                c.id AS comment_id,
                u.id AS owner_id,
                u.fullname,
                u.avatar_url AS avatar,
                c.content,
                c.media_url AS mediaUrl,
                c.post_id,
                c.is_reply,
                c.reply_to_id,
                c.create_time
            FROM feedback AS c
            INNER JOIN user u ON u.id = c.user_id
            WHERE c.reply_to_id = :comment_id and c.del_flag = 0;
                                    """, nativeQuery = true)
    List<Object[]> findReplyWithCommentId(@Param("comment_id") Integer comment_id);

    @Query(value = """
            select
            c.id AS comment_id,
                        u.id AS owner_id,
                        u.fullname,
                        u.avatar_url AS avatar,
                        c.content,
                        c.media_url AS mediaUrl,
                        c.post_id,
                        c.is_reply,
                        c.reply_to_id,
                        c.create_time
                    from feedback as c
                    INNER JOIN
                        user u ON u.id = c.user_id
                        WHERE
                        c.post_id = :post_id and c.del_flag = 0;
                                """, nativeQuery = true)
    Page<Object[]> findCommentWithPostId(@Param("post_id") Integer post_id, Pageable pageable);
}
