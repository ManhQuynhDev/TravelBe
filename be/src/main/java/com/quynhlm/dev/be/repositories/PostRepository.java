package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Post;

public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query(value = "SELECT p.id, p.content, p.status, p.location_id, p.hastag, m.media_url, m.type " +
            "FROM Post p " +
            "INNER JOIN Media m ON p.id = m.post_id", countQuery = "SELECT COUNT(*) FROM Post p INNER JOIN Media m ON p.id = m.post_id", nativeQuery = true)
    Page<Object[]> fetchPostWithMedia(Pageable pageable);

    @Query(value = "SELECT * FROM Post WHERE id= :id", nativeQuery = true)
    Post getAnPost(@Param("id") int id);

    @Query(value = """
            SELECT p.user_id AS owner_id,
                   p.id AS post_id,
                   p.content,
                   m.media_url,
                   p.location_id,
                   p.status,
                   m.type,
                   p.create_time
            FROM Post p
            INNER JOIN Media m ON p.id = m.post_id
            WHERE p.id = :post_id
            """, nativeQuery = true)
    List<Object[]> getPost(@Param("post_id") Integer post_id);

    @Query(value = """
            (SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.content,
                m.media_url,
                p.location_id,
                p.hastag,
                p.status,
                m.type,
                0 AS isShare,
                p.create_time,
                NULL AS share_by_user
            FROM Post p
            INNER JOIN Media m ON p.id = m.post_id)

            UNION ALL

            (SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.content,
                m.media_url,
                p.location_id,
                p.hastag,
                p.status,
                m.type,
                1 AS isShare,
                s.create_time,
                s.user_id AS share_by_user
            FROM Share s
            INNER JOIN Post p ON s.post_id = p.id
            INNER JOIN Media m ON p.id = m.post_id)
            ORDER BY create_time DESC
            """, countQuery = """
            SELECT COUNT(*) FROM (
                (SELECT p.id FROM Post p INNER JOIN Media m ON p.id = m.post_id)
                UNION ALL
                (SELECT p.id FROM Share s INNER JOIN Post p ON s.post_id = p.id INNER JOIN Media m ON p.id = m.post_id)
            ) AS total
            """, nativeQuery = true)
    Page<Object[]> getAllPostsAndSharedPosts(Pageable pageable);

}
