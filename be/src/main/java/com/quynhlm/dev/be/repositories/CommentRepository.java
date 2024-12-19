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

  @Query(value = "SELECT * FROM Comment WHERE post_id = : post_id", nativeQuery = true)
  List<Comment> findCommentWithPostId(@Param("post_id") Integer post_id);

  @Query(value = """
       select
                 DISTINCT
                 c.id as comment_id,
                 u.id as owner_id,
                 u.fullname,
                 u.avatar_url as avatar,
                 c.content,
                 c.post_id,
                 c.share_id,
                 c.create_time,
                 COUNT(DISTINCT cr.id) AS reaction_count
              from comment c
                inner join user u on u.id = c.user_id
                left join comment_reaction cr on cr.comment_id = c.id
                left join reply r on r.comment_id = c.id
                where c.id = :comment_id
                group by c.id , u.id , r.id , cr.id ,c.post_id
      """, nativeQuery = true)
  List<Object[]> findCommentWithId(@Param("comment_id") Integer comment_id);

  Page<Comment> findAll(Pageable pageable);

  @Query(value = """
         select
         DISTINCT
         c.id as comment_id,
         u.id as owner_id,
         u.fullname,
         u.avatar_url as avatar,
         c.content,
         c.post_id,
         c.share_id,
         c.create_time,
         COUNT(DISTINCT cr.id) AS reaction_count,
         MAX(CASE WHEN cr.user_id = :userId THEN cr.type ELSE NULL END) AS user_reaction_type
      from comment c
        inner join user u on u.id = c.user_id
        left join comment_reaction cr on cr.comment_id = c.id
        left join reply r on r.comment_id = c.id
        group by c.id , u.id , r.id , cr.id ,c.post_id
        having c.post_id = :postId
        ORDER BY c.create_time DESC;
                                """, nativeQuery = true)
  Page<Object[]> fetchCommentWithPostId(Pageable pageable, @Param("postId") Integer postId,
      @Param("userId") Integer userId);

  @Query(value = """
         select
           DISTINCT
         c.id as comment_id,
         u.id as owner_id,
         u.fullname,
         u.avatar_url as avatar,
         c.content,
         c.post_id,
         c.share_id,
         c.create_time,
         COUNT(DISTINCT cr.id) AS reaction_count,
         MAX(CASE WHEN cr.user_id = :userId THEN cr.type ELSE NULL END) AS user_reaction_type
      from comment c
        inner join user u on u.id = c.user_id
        left join comment_reaction cr on cr.comment_id = c.id
        left join reply r on r.comment_id = c.id
        group by c.id , u.id , r.id , cr.id
        having c.post_id = :shareId;
                                """, nativeQuery = true)
  Page<Object[]> fetchCommentWithShareId(Pageable pageable, @Param("shareId") Integer shareId,
      @Param("userId") Integer userId);
}