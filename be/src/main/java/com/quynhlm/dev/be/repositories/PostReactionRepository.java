package com.quynhlm.dev.be.repositories;

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
}
