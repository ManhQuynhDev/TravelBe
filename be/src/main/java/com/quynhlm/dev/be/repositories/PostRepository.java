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
    WITH top_posts AS (
        SELECT
            p.id AS post_id
        FROM
            Post p
        LEFT JOIN
            post_reaction r ON p.id = r.post_id
        WHERE
            p.status = 'PUBLIC'
            AND p.user_id NOT IN (:userIds) -- Loại trừ bài viết của bạn bè
        GROUP BY
            p.id
        ORDER BY
            COUNT(DISTINCT r.id) DESC
        LIMIT 10
    )
    SELECT * FROM (
        -- Top 10 bài viết có reaction_count cao nhất (không phải của bạn bè)
        (
            SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.location_id,
                a.fullname AS admin_name,
                a.avatar_url,
                p.content,
                p.hastag,
                p.status,
                m.type,
                0 AS isShare,
                p.create_time,
                NULL AS share_by_user,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
                COUNT(DISTINCT s2.id) AS share_count,
                (SELECT COUNT(*) FROM tag t WHERE t.post_id = p.id) AS isTag
            FROM
                Post p
            INNER JOIN
                Media m ON p.id = m.post_id
            LEFT JOIN
                post_reaction r ON p.id = r.post_id
            LEFT JOIN
                comment c ON p.id = c.post_id
            LEFT JOIN
                share s2 ON p.id = s2.post_id
            LEFT JOIN
                User a ON p.user_id = a.id
            WHERE
                p.status = 'PUBLIC'
                AND p.user_id NOT IN (:userIds)
                AND p.id NOT IN (SELECT post_id FROM top_posts)
            GROUP BY
                p.id, p.user_id, p.content, p.location_id, p.hastag, p.status, m.type, p.create_time, a.fullname, a.avatar_url
            ORDER BY
                COUNT(DISTINCT r.id) DESC
            LIMIT 10
        )
        UNION ALL
        (
            SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.location_id,
                a.fullname AS admin_name,
                a.avatar_url,
                p.content,
                p.hastag,
                p.status,
                m.type,
                0 AS isShare,
                p.create_time,
                NULL AS share_by_user,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
                COUNT(DISTINCT s2.id) AS share_count,
                (SELECT COUNT(*) FROM tag t WHERE t.post_id = p.id) AS isTag
            FROM
                Post p
            INNER JOIN
                Media m ON p.id = m.post_id
            LEFT JOIN
                post_reaction r ON p.id = r.post_id
            LEFT JOIN
                comment c ON p.id = c.post_id AND c.type = 'POST'
            LEFT JOIN
                share s2 ON p.id = s2.post_id
            LEFT JOIN
                User a ON p.user_id = a.id
            WHERE
                p.status = 'PUBLIC' AND a.id IN (:userIds)
                AND p.id NOT IN (SELECT post_id FROM top_posts)
            GROUP BY
                p.id, p.user_id, p.content, p.location_id, p.hastag, p.status, m.type, p.create_time, a.fullname, a.avatar_url
        )
        UNION ALL
        (
            SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.location_id,
                a.fullname AS admin_name,
                a.avatar_url,
                p.content,
                p.hastag,
                p.status,
                m.type,
                1 AS isShare,
                s.create_time,
                s.user_id AS share_by_user,
                COUNT(DISTINCT sr.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
                COUNT(DISTINCT s2.id) AS share_count,
                (SELECT COUNT(*) FROM tag t WHERE t.post_id = p.id) AS isTag
            FROM
                Share s
            INNER JOIN
                Post p ON s.post_id = p.id
            INNER JOIN
                Media m ON p.id = m.post_id
            LEFT JOIN
                share_post_reaction sr ON s.id = sr.share_id
            LEFT JOIN
                comment c ON p.id = c.post_id AND c.type = 'SHARE'
            LEFT JOIN
                share s2 ON p.id = s2.post_id
            LEFT JOIN
                User a ON p.user_id = a.id
            WHERE
                p.status = 'PUBLIC' AND a.id IN (:userIds)
                AND p.id NOT IN (SELECT post_id FROM top_posts)
            GROUP BY
                p.id, p.user_id, p.content, p.location_id, p.hastag, p.status, m.type, p.create_time, a.fullname, a.avatar_url
        )
        UNION ALL
        -- Bài viết của chủ bài viết
        (
            SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.location_id,
                a.fullname AS admin_name,
                a.avatar_url,
                p.content,
                p.hastag,
                p.status,
                m.type,
                0 AS isShare,
                p.create_time,
                NULL AS share_by_user,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
                COUNT(DISTINCT s2.id) AS share_count,
                (SELECT COUNT(*) FROM tag t WHERE t.post_id = p.id) AS isTag
            FROM
                Post p
            INNER JOIN
                Media m ON p.id = m.post_id
            LEFT JOIN
                post_reaction r ON p.id = r.post_id
            LEFT JOIN
                comment c ON p.id = c.post_id
            LEFT JOIN
                share s2 ON p.id = s2.post_id
            LEFT JOIN
                User a ON p.user_id = a.id
            WHERE
                p.user_id = :userId
                AND p.id NOT IN (SELECT post_id FROM top_posts) -- Loại trừ các bài viết trong top 10
            GROUP BY
                p.id, p.user_id, p.content, p.location_id, p.hastag, p.status, m.type, p.create_time, a.fullname, a.avatar_url
        )
    ) AS posts
    ORDER BY create_time DESC
""", nativeQuery = true)
Page<Object[]> getAllPostsExceptFriends(@Param("userIds") List<Integer> userIds, @Param("userId") Integer userId,
        Pageable pageable);

    

    // Get all post type video

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
