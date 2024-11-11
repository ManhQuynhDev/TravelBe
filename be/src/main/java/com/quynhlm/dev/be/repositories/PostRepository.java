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
                NULL AS share_by_user,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
                COUNT(DISTINCT s2.id) AS share_count
            FROM Post p
            INNER JOIN Media m ON p.id = m.post_id
            LEFT JOIN post_reaction r ON p.id = r.post_id
            LEFT JOIN comment c ON p.id = c.post_id
            LEFT JOIN share s2 ON p.id = s2.post_id
            WHERE p.status = 'PUBLIC'
            GROUP BY p.id, m.media_url, p.user_id, p.content, p.location_id, p.hastag, p.status, m.type, p.create_time)

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
                s.user_id AS share_by_user,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
                COUNT(DISTINCT s2.id) AS share_count
            FROM Share s
            INNER JOIN Post p ON s.post_id = p.id
            INNER JOIN Media m ON p.id = m.post_id
            LEFT JOIN post_reaction r ON p.id = r.post_id
            LEFT JOIN comment c ON p.id = c.post_id
            LEFT JOIN share s2 ON p.id = s2.post_id
            WHERE p.status = 'PUBLIC'
            GROUP BY p.id, m.media_url, p.user_id, p.content, p.location_id, p.hastag, p.status, m.type, s.create_time, s.user_id)
            ORDER BY create_time DESC
            """, countQuery = """
            SELECT COUNT(*) FROM (
                (SELECT p.id
                FROM Post p
                INNER JOIN Media m ON p.id = m.post_id
                WHERE p.status = 'PUBLIC')
                UNION ALL
                (SELECT p.id
                FROM Share s
                INNER JOIN Post p ON s.post_id = p.id
                INNER JOIN Media m ON p.id = m.post_id
                WHERE p.status = 'PUBLIC')
            ) AS total
            """, nativeQuery = true)
    Page<Object[]> getAllPostsAndSharedPosts(Pageable pageable);

    @Query(value = """
            SELECT
                p.id AS post_id,
                u.id as owner_id,
                p.location_id,
                p.content,
                p.status,
                u.fullname AS fullname,
                u.avatar_url as avatar,
                m.media_url AS video,
                p.create_time,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
                COUNT(DISTINCT s.id) AS share_count
            FROM
                post p
            INNER JOIN
                user u ON p.user_id = u.id
            INNER JOIN
                media m ON p.id = m.post_id AND m.type = 'VIDEO'
            LEFT JOIN
                post_reaction r ON p.id = r.post_id
            LEFT JOIN
                comment c ON p.id = c.post_id
            LEFT JOIN
                share s ON p.id = s.post_id
            GROUP BY
                p.id , m.media_url , u.id
            """, nativeQuery = true)
    Page<Object[]> fetchPostWithMediaTypeVideo(Pageable pageable);

}
