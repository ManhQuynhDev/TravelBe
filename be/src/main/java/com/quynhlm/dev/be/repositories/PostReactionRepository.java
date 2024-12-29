package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.PostReaction;

public interface PostReactionRepository extends JpaRepository<PostReaction, Integer> {
    @Query(value = """
            select * from post_reaction where post_id = :post_id AND user_id = :user_id
            """, nativeQuery = true)
    PostReaction getAnReactionWithUserIdAndPostId(@Param("post_id") Integer post_id, @Param("user_id") Integer user_id);

    @Query(value = """
            SELECT
                u.id AS owner_id,
                u.fullname AS fullname,
                u.avatar_url AS avatar,
                p.type,
                p.create_time
            FROM post_reaction p
            INNER JOIN user u ON p.user_id = u.id
            WHERE p.type = :type AND p.post_id =:id
            """, nativeQuery = true)
    Page<Object[]> getUserReactionByType(Pageable pageable, @Param("type") String type, @Param("id") Integer id);

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
                    COUNT(pr.type) AS reaction_count
                FROM
                    reactions r
                LEFT JOIN
                    post_reaction pr
                ON
                    r.type = pr.type AND pr.post_id = :post_id
                GROUP BY
                    r.type
            )
            SELECT
                type,
                reaction_count
            FROM
                reaction_counts
            ORDER BY
                reaction_count DESC,
                type
            LIMIT 3;
            """, nativeQuery = true)
    List<Object[]> findTopReactions(@Param("post_id") Integer post_id);

}
