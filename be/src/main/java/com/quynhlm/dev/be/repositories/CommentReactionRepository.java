package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.CommentReaction;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Integer> {
    CommentReaction findByCommentIdAndUserId(int commentId, int userId);

    @Query(value = """
            SELECT
                u.id AS owner_id,
                u.fullname AS fullname,
                u.avatar_url AS avatar,
                c.type,
                c.create_time
            FROM comment_reaction c
            INNER JOIN user u ON c.user_id = u.id
            WHERE c.type = :type AND c.comment_id =:id
            """, nativeQuery = true)
    Page<Object[]> getUserReactionByType(Pageable pageable, @Param("type") String type, @Param("id") Integer id);

    @Query(value = """
             SELECT
                comment_id,
                SUM(CASE WHEN type = 'LIKE' THEN 1 ELSE 0 END) AS like_count,
                SUM(CASE WHEN type = 'LOVE' THEN 1 ELSE 0 END) AS love_count,
                SUM(CASE WHEN type = 'HAHA' THEN 1 ELSE 0 END) AS haha_count,
                SUM(CASE WHEN type = 'WOW' THEN 1 ELSE 0 END) AS wow_count,
                SUM(CASE WHEN type = 'SAD' THEN 1 ELSE 0 END) AS sad_count,
                SUM(CASE WHEN type = 'ANGRY' THEN 1 ELSE 0 END) AS angry_count
            FROM
                comment_reaction
            WHERE
                comment_id = :comment_id
            GROUP BY
                comment_id;
            """, nativeQuery = true)
    List<Object[]> reactionTypeCount(@Param("comment_id") Integer comment_id);

    @Query(value = """
            WITH reactions AS (
                SELECT 'LIKE' AS type
                UNION ALL SELECT 'LOVE'
                UNION ALL SELECT 'HAHA'
                UNION ALL SELECT 'WOW'
                UNION ALL SELECT 'SAD'
                UNION ALL SELECT 'ANGRY'
            ),
            reaction_counts AS (
                SELECT
                    r.type,
                    COUNT(cr.type) AS reaction_count
                FROM
                    reactions r
                LEFT JOIN
                    comment_reaction cr
                ON
                    r.type = cr.type AND cr.comment_id = :comment_id
                GROUP BY
                    r.type
            )
            SELECT
                type,
                reaction_count
            FROM
                reaction_counts
            WHERE reaction_count > 0
            ORDER BY
                reaction_count DESC,
                type
                
            """, nativeQuery = true)
    List<Object[]> findTopReactions(@Param("comment_id") Integer comment_id);
}
