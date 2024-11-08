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
            select u.id , u.fullname as fullname , u.avatar_url as avatar, p.type, p.create_time from post_reaction p
            inner join user u ON p.user_id = u.id
            where p.type = :type);
            """, nativeQuery = true)
    Page<Object[]> getUserByType(Pageable pageable, @Param("type") String type);
}
