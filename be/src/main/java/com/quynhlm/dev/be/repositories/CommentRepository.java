package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.quynhlm.dev.be.model.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @Query(value = "SELECT COUNT(*) FROM Comment WHERE post_id = :post_id and del_flag = 0", nativeQuery = true)
    Integer commentCountWithPostId(@Param("post_id") Integer post_id);

    @Query(value = "SELECT * FROM Comment WHERE id = :id and del_flag = 0", nativeQuery = true)
    Comment findComment(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE Comment f SET f.delFlag = 1 WHERE f.id = :feedbackId OR f.replyToId = :feedbackId")
    void softDeleteFeedbackAndReplies(Integer feedbackId);

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
                c.create_time,
                COUNT(r.id) AS reaction_count,
                MAX(CASE WHEN r.user_id = :user_id THEN r.type ELSE NULL END) AS user_reaction_type
            FROM comment AS c
            INNER JOIN user u ON u.id = c.user_id
            LEFT JOIN comment_reaction r ON r.comment_id = c.id
            WHERE c.id = :comment_id and c.del_flag = 0
            GROUP BY
                c.id, u.id, u.fullname, u.avatar_url, c.content, c.media_url, c.post_id, c.is_reply, c.reply_to_id, c.create_time;
                                """, nativeQuery = true)
    List<Object[]> findCommentWithId(@Param("comment_id") Integer comment_id, @Param("user_id") Integer user_id);

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
                c.create_time,
                COUNT(r.id) AS reaction_count,
                MAX(CASE WHEN r.user_id = :user_id THEN r.type ELSE NULL END) AS user_reaction_type
            FROM comment AS c
            INNER JOIN user u ON u.id = c.user_id
            LEFT JOIN comment_reaction r ON r.comment_id = c.id
            WHERE c.reply_to_id = :comment_id and c.del_flag = 0
            GROUP BY
                c.id, u.id, u.fullname, u.avatar_url, c.content, c.media_url, c.post_id, c.is_reply, c.reply_to_id, c.create_time;
                                    """, nativeQuery = true)
    List<Object[]> findReplyWithCommentId(@Param("comment_id") Integer comment_id, @Param("user_id") Integer user_id);

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
                c.create_time,
                COUNT(r.id) AS reaction_count,
                MAX(CASE WHEN r.user_id = :user_id THEN r.type ELSE NULL END) AS user_reaction_type
            FROM comment AS c
            INNER JOIN user u ON u.id = c.user_id
            LEFT JOIN comment_reaction r ON r.comment_id = c.id
            WHERE c.post_id = :post_id AND c.del_flag = 0
            GROUP BY
                c.id, u.id, u.fullname, u.avatar_url, c.content, c.media_url, c.post_id, c.is_reply, c.reply_to_id, c.create_time;

                                                        """, nativeQuery = true)
    Page<Object[]> findCommentWithPostId(@Param("post_id") Integer post_id, @Param("user_id") Integer user_id,
            Pageable pageable);
}
