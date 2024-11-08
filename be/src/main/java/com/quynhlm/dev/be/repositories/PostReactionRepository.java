package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.PostReaction;

public interface PostReactionRepository extends JpaRepository<PostReaction, Integer> {
    PostReaction findByPostIdAndUserId(int postId, int userId);

    @Query(value = """
            SELECT
                u.id AS owner_id,
                u.fullname AS fullname,
                u.avatar_url AS avatar,
                p.type,
                p.create_time
            FROM post_reaction p
            INNER JOIN user u ON p.user_id = u.id
            WHERE p.type = :type
            """, countQuery = """
            SELECT COUNT(*)
            FROM post_reaction p
            INNER JOIN user u ON p.user_id = u.id
            WHERE p.type = :type
            """, nativeQuery = true)
    Page<Object[]> getUserReactionByType(Pageable pageable, @Param("type") String type);

}
