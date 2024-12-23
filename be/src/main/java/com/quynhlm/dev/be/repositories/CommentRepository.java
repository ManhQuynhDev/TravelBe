package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @Query(value = "SELECT DISTINCT * FROM Comment WHERE id = :id", nativeQuery = true)
    Comment findComment(@Param("id") Integer id);

    List<Comment> findByPostId(Integer postId);
    @Query(value = """
            SELECT
                COUNT(DISTINCT c.id) + COUNT(DISTINCT r.id) AS comment_count
            FROM
                comment c
            LEFT JOIN
                reply r ON c.id = r.comment_id
            WHERE
                c.post_id = :postId;
                """, nativeQuery = true)
    Integer commentCountWithPostId(@Param("postId") Integer postId);

    @Query(value = "SELECT * FROM Comment WHERE post_id = : post_id", nativeQuery = true)
    List<Comment> findCommentWithPostId(@Param("post_id") Integer post_id);

    @Query(value = """
             SELECT
                c.id AS comment_id,
                u.id AS owner_id,
                u.fullname,
                u.avatar_url AS avatar,
                c.content,
                c.media_url AS mediaUrl,
                c.post_id,
                c.create_time,
                COUNT(DISTINCT cr.id) AS reaction_count,
                MAX(CASE WHEN cr.user_id = :userId THEN cr.type ELSE NULL END) AS user_reaction_type
            FROM
                comment c
            INNER JOIN
                user u ON u.id = c.user_id
            LEFT JOIN
                comment_reaction cr ON cr.comment_id = c.id
            WHERE
                c.id = :comment_id
            GROUP BY
                c.id, u.id, c.content, c.post_id, c.create_time, u.fullname, u.avatar_url
            """, nativeQuery = true)
    List<Object[]> findCommentWithId(@Param("comment_id") Integer comment_id, @Param("userId") Integer userId);

    Page<Comment> findAll(Pageable pageable);

    @Query(value = """
               SELECT
                c.id AS comment_id,
                u.id AS owner_id,
                u.fullname,
                u.avatar_url AS avatar,
                c.content,
                c.media_url AS mediaUrl,
                c.post_id,
                c.create_time,
                COUNT(DISTINCT cr.id) AS reaction_count,
                MAX(CASE WHEN cr.user_id = :userId THEN cr.type ELSE NULL END) AS user_reaction_type
            FROM
                comment c
            INNER JOIN
                user u ON u.id = c.user_id
            LEFT JOIN
                comment_reaction cr ON cr.comment_id = c.id
            WHERE
                c.post_id = :postId
            GROUP BY
                c.id, u.id, c.content, c.post_id, c.create_time, u.fullname, u.avatar_url
            ORDER BY    
                c.create_time DESC;
                                            """, nativeQuery = true)
    Page<Object[]> fetchCommentWithPostId(Pageable pageable, @Param("postId") Integer postId,
            @Param("userId") Integer userId);
}