package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @Query(value = "SELECT * FROM Comment WHERE id = :id", nativeQuery = true)
    Comment findComment(@Param("id") Integer id);

    Page<Comment> findAll(Pageable pageable);

    @Query(value = """
                select
                c.id as comment_id,
                u.id as owner_id,
                u.fullname,           
                u.avatar_url as avatar,
                c.content,
                c.post_id,
                c.create_time,
               	COUNT(DISTINCT cr.id) AS reaction_count,
                COUNT(DISTINCT r.id) AS reply_count
            	from comment c
               inner join user u on u.id = c.user_id
               left join comment_reaction cr on cr.comment_id = c.id
               left join reply r on r.comment_id = c.id
               group by c.id , u.id , r.id , cr.id
               having c.post_id = :postId;
                                       """, nativeQuery = true)
    Page<Object[]> fetchCommentWithPostId(Pageable pageable, @Param("postId") Integer postId);
}