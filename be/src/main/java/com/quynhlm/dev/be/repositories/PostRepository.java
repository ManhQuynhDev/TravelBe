package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Post;

public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query(value = "SELECT DISTINCT p.id, p.content, p.status, p.location_id, p.hastag, m.media_url, m.type " +
            "FROM Post p " +
            "INNER JOIN Media m ON p.id = m.post_id", countQuery = "SELECT COUNT(*) FROM Post p INNER JOIN Media m ON p.id = m.post_id", nativeQuery = true)
    Page<Object[]> fetchPostWithMedia(Pageable pageable);

    @Query(value = "SELECT DISTINCT * FROM Post WHERE id= :id", nativeQuery = true)
    Post getAnPost(@Param("id") int id);

    @Query(value = """
                    SELECT
                    DISTINCT
                        u.id as owner_id,
                        p.id as post_id,
                        p.location_id,
                        l.address,
                        p.content,
                        p.status,
                        u.fullname AS fullname,
                        u.avatar_url as avatar,
                        m.type,
                        p.create_time,
                        COUNT(DISTINCT r.id) AS reaction_count,
                        (
            	SELECT COUNT(*)
            	FROM comment c
            	WHERE c.type = 'POST' AND c.post_id = p.id
            ) AS comment_count,
                        COUNT(DISTINCT s.id) AS share_count,
                          MAX(CASE WHEN r.user_id = :userId THEN r.type ELSE NULL END) AS user_reaction_type
                    FROM
                        post p
                    INNER JOIN
                        user u ON p.user_id = u.id
                    INNER JOIN
                        location l ON l.id = p.location_id
                    INNER JOIN
                        media m ON p.id = m.post_id
                    LEFT JOIN
                        post_reaction r ON p.id = r.post_id
                    LEFT JOIN
                        comment c ON p.id = c.post_id
                    LEFT JOIN
                        share s ON p.id = s.post_id
                    WHERE p.id = :post_id
                    GROUP BY
                        p.id , u.id , m.type
                    """, nativeQuery = true)
    List<Object[]> getPost(@Param("post_id") Integer post_id, @Param("userId") Integer userId);

    @Query(value = """
                    SELECT
                    DISTINCT
                        u.id as owner_id,
                        p.id AS post_id,
                        p.location_id,
                        l.address,
                        p.content,
                        p.status,
                        u.fullname AS fullname,
                        u.avatar_url as avatar,
                        m.type,
                        p.create_time,
                        COUNT(DISTINCT r.id) AS reaction_count,
                        (
                SELECT COUNT(*)
                FROM comment c
                WHERE c.type = 'POST' AND c.post_id = p.id
            ) AS comment_count,
                        COUNT(DISTINCT s.id) AS share_count
                    FROM
                        post p
                    INNER JOIN
                        user u ON p.user_id = u.id
                    INNER JOIN
                        location l ON l.id = p.location_id
                    INNER JOIN
                        media m ON p.id = m.post_id
                    LEFT JOIN
                        post_reaction r ON p.id = r.post_id
                    LEFT JOIN
                        comment c ON p.id = c.post_id
                    LEFT JOIN
                        share s ON p.id = s.post_id
                    WHERE p.id = :post_id
                    GROUP BY
                        p.id , u.id , m.type
                    """, nativeQuery = true)
    List<Object[]> getPostSave(@Param("post_id") Integer post_id);

    @Query(value = """
                    (SELECT
                    DISTINCT
                        p.user_id AS owner_id,
                        p.id AS post_id,
                        p.content,
                        m.media_url,
                        p.location_id,
                        p.status,
                        m.type,
                        0 AS isShare,
                        p.create_time,
                        NULL AS share_by_user,
                        COUNT(DISTINCT r.id) AS reaction_count,
                        (
            	SELECT COUNT(*)
            	FROM comment c
            	WHERE c.type = 'SHARE' AND c.post_id = p.id
            ) AS comment_count,
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
                    DISTINCT
                        p.user_id AS owner_id,
                        p.id AS post_id,
                        p.content,
                        m.media_url,
                        p.location_id,
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
                        p.user_id AS owner_id,
                        p.id AS post_id,
                        p.location_id,
                        l.address,
                        a.fullname AS admin_name,
                        a.avatar_url,
                        p.content,
                        p.status,
                        m.type,
                        0 AS isShare,
                        p.create_time,
                        NULL AS share_by_user,
                        COUNT(DISTINCT r.id) AS reaction_count,
                        COUNT(DISTINCT c.id) AS comment_count,
                        COUNT(DISTINCT s2.id) AS share_count,
                        COUNT(t.id) AS isTag,
                        MAX(CASE WHEN r.user_id = :userId THEN r.type ELSE NULL END) AS user_reaction_type
                    FROM
                        Post p
                    INNER JOIN
                        Media m ON p.id = m.post_id
                    INNER JOIN
                        Location l ON l.id = p.location_id
                    LEFT JOIN
                        post_reaction r ON p.id = r.post_id
                    LEFT JOIN
                        comment c ON p.id = c.post_id
                    LEFT JOIN
                        share s2 ON p.id = s2.post_id
                    LEFT JOIN
                        User a ON p.user_id = a.id
                    LEFT JOIN
                        tag t ON t.post_id = p.id
                    WHERE
                        p.status = 'PUBLIC'
                    GROUP BY
                        p.id, p.user_id, p.content, p.location_id, p.status, m.type, p.create_time, a.fullname, a.avatar_url
            """, nativeQuery = true)
    Page<Object[]> getAllPostsExceptFriends(@Param("userId") Integer userId,
            Pageable pageable);

    // Get all post type video
    @Query(value = """
            SELECT
                DISTINCT
                p.id AS post_id,
                u.id as owner_id,
                p.location_id,
                l.address,
                p.content,
                p.status,
                u.fullname AS fullname,
                u.avatar_url as avatar,
                m.media_url AS video,
                p.create_time,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
                COUNT(DISTINCT s.id) AS share_count,
                MAX(CASE WHEN r.user_id = :user_id THEN r.type ELSE NULL END) AS user_reaction_type
            FROM
                post p
            INNER JOIN
                user u ON p.user_id = u.id
            INNER JOIN
                location l ON l.id = p.location_id
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
    Page<Object[]> fetchPostWithMediaTypeVideo(@Param("user_id") Integer user_id, Pageable pageable);

    @Query(value = """
            SELECT
                    DISTINCT
                u.id AS owner_id,
                p.id AS post_id,
                p.location_id,
                l.address,
                p.content,
                p.status,
                u.fullname AS fullname,
                u.avatar_url AS avatar,
                p.create_time,
                COALESCE(reaction_count.reaction_count, 0) AS reaction_count,
                COALESCE(comment_count.comment_count, 0) AS comment_count,
                COALESCE(share_count.share_count, 0) AS share_count,
                MAX(CASE WHEN r.user_id = :user_id THEN r.type ELSE NULL END) AS user_reaction_type
            FROM hash_tag h
            INNER JOIN Post p ON p.id = h.post_id
            INNER JOIN User u ON u.id = p.user_id
            INNER JOIN Location l ON l.id = p.location_id
            LEFT JOIN (
                SELECT post_id, COUNT(*) AS reaction_count
                FROM post_reaction
                GROUP BY post_id
            ) reaction_count ON reaction_count.post_id = p.id
            LEFT JOIN (
                SELECT post_id, COUNT(*) AS comment_count
                FROM comment
                WHERE type = 'POST'
                GROUP BY post_id
            ) comment_count ON comment_count.post_id = p.id
            LEFT JOIN (
                SELECT post_id, COUNT(*) AS share_count
                FROM share
                GROUP BY post_id
            ) share_count ON share_count.post_id = p.id
            LEFT JOIN post_reaction r ON r.post_id = p.id
            WHERE h.hashtag = :q
            GROUP BY
                p.id, u.id, l.address, p.content, p.status, u.fullname, u.avatar_url, p.create_time;
                        """, nativeQuery = true)
    Page<Object[]> searchByHashTag(@Param("q") String keyword, @Param("user_id") Integer user_id, Pageable pageable);

    @Query(value = """
            SELECT
                DISTINCT
                p.id AS post_id,
                u.id as owner_id,
                p.location_id,
                l.address,
                p.content,
                p.status,
                u.fullname AS fullname,
                u.avatar_url as avatar,
                m.type,
                p.create_time,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
                COUNT(DISTINCT s.id) AS share_count,
                MAX(CASE WHEN r.user_id = :user_id THEN r.type ELSE NULL END) AS user_reaction_type
            FROM
                post p
            INNER JOIN
                user u ON p.user_id = u.id
            INNER JOIN
                location l ON l.id = p.location_id
            INNER JOIN
                media m ON p.id = m.post_id
            LEFT JOIN
                post_reaction r ON p.id = r.post_id
            LEFT JOIN
                comment c ON p.id = c.post_id
            LEFT JOIN
                share s ON p.id = s.post_id
            GROUP BY
                p.id, u.id, p.location_id, p.content, p.status, u.fullname, u.avatar_url, m.type, p.create_time
            """, nativeQuery = true)
    Page<Object[]> fetchAllPost(@Param("user_id") Integer user_id, Pageable pageable);

    @Query(value = """
            SELECT
            DISTINCT
                        p.id AS post_id,
                        u.id as owner_id,
                        p.location_id,
                        l.address,
                        p.content,
                        p.status,
                        u.fullname AS fullname,
                        u.avatar_url as avatar,
                        m.type,
                        p.create_time,
                        COUNT(DISTINCT r.id) AS reaction_count,
                        (
            	SELECT COUNT(*)
            	FROM comment c
            	WHERE c.type = 'POST' AND c.post_id = p.id
            ) AS comment_count,
                        COUNT(DISTINCT s.id) AS share_count,
                        MAX(CASE WHEN r.user_id = :user_id THEN r.type ELSE NULL END) AS user_reaction_type
                    FROM
                        post p
                    INNER JOIN
                        user u ON p.user_id = u.id
                    INNER JOIN
                        location l ON l.id = p.location_id
                    INNER JOIN
                        media m ON p.id = m.post_id
                    LEFT JOIN
                        post_reaction r ON p.id = r.post_id
                    LEFT JOIN
                        comment c ON p.id = c.post_id
                    LEFT JOIN
                        share s ON p.id = s.post_id
                    WHERE u.id = :user_id
                    GROUP BY
                        p.id , m.media_url , u.id , m.type
                    ORDER BY p.create_time DESC;
            """, nativeQuery = true)
    Page<Object[]> foundPostByUserId(@Param("user_id") Integer user_id, Pageable pageable);

    @Query(value = """
                    SELECT
                    DISTINCT
                        u.id as owner_id,
                        p.id as post_id,
                        p.location_id,
                        l.address,
                        p.content,
                        p.status,
                        u.fullname AS fullname,
                        u.avatar_url as avatar,
                        m.type,
                        p.create_time,
                        COUNT(DISTINCT r.id) AS reaction_count,
                        (
                SELECT COUNT(*)
                FROM comment c
                WHERE c.type = 'POST' AND c.post_id = p.id
            ) AS comment_count,
                        COUNT(DISTINCT s.id) AS share_count,
                          MAX(CASE WHEN r.user_id = :userId THEN r.type ELSE NULL END) AS user_reaction_type
                    FROM
                        post p
                    INNER JOIN
                        user u ON p.user_id = u.id
                    INNER JOIN
                        location l ON l.id = p.location_id
                    INNER JOIN
                        media m ON p.id = m.post_id
                    LEFT JOIN
                        post_reaction r ON p.id = r.post_id
                    LEFT JOIN
                        comment c ON p.id = c.post_id
                    LEFT JOIN
                        share s ON p.id = s.post_id
                    WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :q, '%'))
                    GROUP BY
                        p.id , u.id , m.type
                    """, nativeQuery = true)
    Page<Object[]> searchPostWithContent(@Param("q") String keyword, @Param("userId") Integer userId,
            Pageable pageable);
}
