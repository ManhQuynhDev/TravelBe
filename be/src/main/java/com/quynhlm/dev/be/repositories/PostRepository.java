package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Post;

public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query(value = "SELECT DISTINCT * FROM Post WHERE id= :id AND delflag = 0", nativeQuery = true)
    Post getAnPost(@Param("id") int id);

    @Query(value = "SELECT DISTINCT * FROM Post WHERE id= :id", nativeQuery = true)
    Post findAnPostById(@Param("id") int id);


    @Query(value = "SELECT DISTINCT * FROM Post WHERE id= :id AND delflag = 1", nativeQuery = true)
    Post getAnPostRestore(@Param("id") int id);

    @Query(value = """
                SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.location_id,
                l.address,
                u.fullname AS admin_name,
                u.avatar_url,
                p.content,
                p.status,
                p.create_time,
                p.is_share,
                p.share_by_id,
                us.fullname AS user_share_name,
                us.avatar_url AS user_share_avatar,
                p.share_content,
                p.share_time,
                p.status_share,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
            	(SELECT COUNT(*) FROM post WHERE is_share = 1 AND id = p.id) AS share_count,
                MAX(CASE WHEN r.user_id = :userId THEN r.type ELSE NULL END) AS user_reaction_type,
                p.post_id
            FROM
                Post p
            INNER JOIN
                Location l ON l.id = p.location_id
            INNER JOIN
                User u ON u.id = p.user_id
            LEFT JOIN
                User us ON us.id = p.share_by_id
            LEFT JOIN
                post_reaction r ON p.id = r.post_id
            LEFT JOIN
            	comment c ON c.id = p.post_id
            WHERE
                p.id =:postId AND p.status = 'PUBLIC' AND p.delflag = 0
            GROUP BY
                p.id, p.user_id,p.post_id, p.content, p.location_id, p.status, p.is_share, p.share_time, p.share_by_id, u.fullname, u.avatar_url, l.address, us.fullname, us.avatar_url;
                        """, nativeQuery = true)
    List<Object[]> getPostWithPostId(@Param("postId") Integer postId, @Param("userId") Integer userId);

    @Query(value = """
                SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.location_id,
                l.address,
                u.fullname AS admin_name,
                u.avatar_url,
                p.content,
                p.status,
                p.create_time,
                p.is_share,
                p.share_by_id,
                us.fullname AS user_share_name,
                us.avatar_url AS user_share_avatar,
                p.share_content,
                p.share_time,
                p.status_share,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
            	(SELECT COUNT(*) FROM post WHERE is_share = 1 AND id = p.id) AS share_count,
                p.post_id
            FROM
                Post p
            INNER JOIN
                Location l ON l.id = p.location_id
            INNER JOIN
                User u ON u.id = p.user_id
            LEFT JOIN
                User us ON us.id = p.share_by_id
            LEFT JOIN
                post_reaction r ON p.id = r.post_id
            LEFT JOIN
            	comment c ON c.id = p.id
            WHERE
                p.id =:postId AND p.status = 'PUBLIC' AND p.delflag = 0
            GROUP BY
                p.id, p.user_id, p.content , p.post_id, p.location_id, p.status, p.is_share, p.share_time, p.share_by_id, u.fullname, u.avatar_url, l.address, us.fullname, us.avatar_url;
                        """, nativeQuery = true)
    List<Object[]> getPostSave(@Param("postId") Integer postId);

    @Query(value = """
                SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.location_id,
                l.address,
                u.fullname AS admin_name,
                u.avatar_url,
                p.content,
                p.status,
                p.create_time,
                p.is_share,
                p.share_by_id,
                us.fullname AS user_share_name,
                us.avatar_url AS user_share_avatar,
                p.share_content,
                p.share_time,
                p.status_share,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
            	(SELECT COUNT(*) FROM post WHERE is_share = 1 AND id = p.id) AS share_count,
                MAX(CASE WHEN r.user_id = :userId THEN r.type ELSE NULL END) AS user_reaction_type,
                p.post_id
            FROM
                Post p
            INNER JOIN
                Location l ON l.id = p.location_id
            INNER JOIN
                User u ON u.id = p.user_id
            LEFT JOIN
                User us ON us.id = p.share_by_id
            LEFT JOIN
                post_reaction r ON p.id = r.post_id
            LEFT JOIN
            	comment c ON c.id = p.post_id
            WHERE
                p.status = 'PUBLIC' AND p.delflag = 0
            GROUP BY
                p.id, p.user_id, p.content,p.post_id, p.location_id, p.status, p.is_share, p.share_time, p.share_by_id, u.fullname, u.avatar_url, l.address, us.fullname, us.avatar_url
            ORDER BY p.create_time DESC;
                        """, nativeQuery = true)
    Page<Object[]> getAllPostsAndSharedPosts(@Param("friendIds") List<Integer> friendIds , @Param("userId") Integer userId, Pageable pageable);

    @Query(value = """
                SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.location_id,
                l.address,
                u.fullname AS admin_name,
                u.avatar_url,
                p.content,
                p.status,
                p.create_time,
                p.is_share,
                p.share_by_id,
                us.fullname AS user_share_name,
                us.avatar_url AS user_share_avatar,
                p.share_content,
                p.share_time,
                p.status_share,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
            	(SELECT COUNT(*) FROM post WHERE is_share = 1 AND id = p.id) AS share_count,
                MAX(CASE WHEN r.user_id = 1 THEN r.type ELSE NULL END) AS user_reaction_type,
                p.post_id,
                p.delflag
            FROM
                Post p
            INNER JOIN
                Location l ON l.id = p.location_id
            INNER JOIN
                User u ON u.id = p.user_id
            LEFT JOIN
                User us ON us.id = p.share_by_id
            LEFT JOIN
                post_reaction r ON p.id = r.post_id
            LEFT JOIN
            	comment c ON c.id = p.post_id
            WHERE
                p.status = 'PUBLIC'
            GROUP BY
                p.id, p.user_id, p.content,p.post_id, p.location_id, p.status, p.is_share, p.share_time, p.share_by_id, u.fullname, u.avatar_url, l.address, us.fullname, us.avatar_url
            ORDER BY p.create_time DESC;
                        """, nativeQuery = true)
    Page<Object[]> getAllPost(Pageable pageable);

    @Query(value = """
                SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.location_id,
                l.address,
                u.fullname AS admin_name,
                u.avatar_url,
                p.content,
                p.status,
                p.create_time,
                p.is_share,
                p.share_by_id,
                us.fullname AS user_share_name,
                us.avatar_url AS user_share_avatar,
                p.share_content,
                p.share_time,
                p.status_share,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
            	(SELECT COUNT(*) FROM post WHERE is_share = 1 AND id = p.id) AS share_count,
                MAX(CASE WHEN r.user_id = :userId THEN r.type ELSE NULL END) AS user_reaction_type
            FROM
                Post p
            INNER JOIN
                Location l ON l.id = p.location_id
            INNER JOIN
                User u ON u.id = p.user_id
            INNER JOIN
                Media m ON p.id = m.post_id
            LEFT JOIN
                User us ON us.id = p.share_by_id
            LEFT JOIN
                post_reaction r ON p.id = r.post_id
            LEFT JOIN
            	comment c ON c.id = p.post_id
            WHERE
                p.status = 'PUBLIC' and m.type = 'VIDEO' and p.is_share = 0  AND p.delflag = 0
            GROUP BY
                p.id, p.user_id, p.content, p.location_id, p.status, p.is_share, p.share_time, p.share_by_id, u.fullname, u.avatar_url, l.address, us.fullname, us.avatar_url
            ORDER BY p.create_time DESC;
                        """, nativeQuery = true)
    Page<Object[]> fetchPostWithMediaTypeVideo(@Param("userId") Integer userId, Pageable pageable);

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
                SELECT DISTINCT
                    p.id AS post_id,
                    u.id AS owner_id,
                    p.location_id,
                    l.address,
                    p.content,
                    p.status,
                    u.fullname AS fullname,
                    u.avatar_url AS avatar,
                    m.type,
                    p.create_time,
                    COUNT(DISTINCT r.id) AS reaction_count,
                    COUNT(DISTINCT c.id) AS comment_count,
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
                GROUP BY
                    p.id, u.id, p.location_id, l.address, p.content, p.status, u.fullname, u.avatar_url, m.type, p.create_time
                ORDER BY
                    reaction_count DESC
            """, nativeQuery = true)
    Page<Object[]> statisticalPost(Pageable pageable);

    @Query(value = """
                        SELECT
                p.user_id AS owner_id,
                p.id AS post_id,
                p.location_id,
                l.address,
                u.fullname AS admin_name,
                u.avatar_url,
                p.content,
                p.status,
                p.create_time,
                p.is_share,
                p.share_by_id,
                us.fullname AS user_share_name,
                us.avatar_url AS user_share_avatar,
                p.share_content,
                p.share_time,
                p.status_share,
                COUNT(DISTINCT r.id) AS reaction_count,
                COUNT(DISTINCT c.id) AS comment_count,
            	(SELECT COUNT(*) FROM post WHERE is_share = 1 AND id = p.id) AS share_count,
                MAX(CASE WHEN r.user_id = :userId THEN r.type ELSE NULL END) AS user_reaction_type,
                p.post_id
            FROM
                Post p
            INNER JOIN
                Location l ON l.id = p.location_id
            INNER JOIN
                User u ON u.id = p.user_id
            LEFT JOIN
                User us ON us.id = p.share_by_id
            LEFT JOIN
                post_reaction r ON p.id = r.post_id
            LEFT JOIN
            	comment c ON c.id = p.post_id
            WHERE
                p.status = 'PUBLIC' AND p.user_id = :userId AND p.delflag = 0
            GROUP BY
                p.id, p.user_id,p.post_id, p.content, p.location_id, p.status, p.is_share, p.share_time, p.share_by_id, u.fullname, u.avatar_url, l.address, us.fullname, us.avatar_url
            ORDER BY p.create_time DESC;
                        """, nativeQuery = true)
    Page<Object[]> foundPostByUserId(@Param("userId") Integer userId, Pageable pageable);

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

    @Query(value = """
                WITH months AS (
                SELECT 1 AS month UNION ALL
                SELECT 2 UNION ALL
                SELECT 3 UNION ALL
                SELECT 4 UNION ALL
                SELECT 5 UNION ALL
                SELECT 6 UNION ALL
                SELECT 7 UNION ALL
                SELECT 8 UNION ALL
                SELECT 9 UNION ALL
                SELECT 10 UNION ALL
                SELECT 11 UNION ALL
                SELECT 12
            )
            SELECT
                m.month AS month_number,
                COALESCE(COUNT(p.id), 0) AS post_count
            FROM months m
            LEFT JOIN (
                SELECT
                    MONTH(create_time) AS month,
                    YEAR(create_time) AS year,
                    id
                FROM post
                WHERE YEAR(create_time) = :year
            ) p ON m.month = p.month
            GROUP BY m.month
            ORDER BY m.month;
                        """, nativeQuery = true)
    List<Object[]> PostCreateInMonth(@Param("year") Integer year);
}
