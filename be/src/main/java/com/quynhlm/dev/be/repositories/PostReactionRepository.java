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
             SELECT 
                post_id,
                SUM(CASE WHEN type = 'LIKE' THEN 1 ELSE 0 END) AS like_count,
                SUM(CASE WHEN type = 'LOVE' THEN 1 ELSE 0 END) AS love_count,
                SUM(CASE WHEN type = 'HAHA' THEN 1 ELSE 0 END) AS haha_count,
                SUM(CASE WHEN type = 'WOW' THEN 1 ELSE 0 END) AS wow_count,
                SUM(CASE WHEN type = 'SAD' THEN 1 ELSE 0 END) AS sad_count,
                SUM(CASE WHEN type = 'ANGRY' THEN 1 ELSE 0 END) AS angry_count
            FROM 
                post_reaction
            WHERE 
                post_id = :post_id
            GROUP BY 
                post_id;
            """, nativeQuery = true)
    List<Object[]> reactionTypeCount(@Param("post_id") Integer post_id);
}
