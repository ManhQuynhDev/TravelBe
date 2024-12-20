package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Share;

public interface ShareRepository extends JpaRepository<Share, Integer> {
    @Query(value = "SELECT * FROM Share WHERE id = :id", nativeQuery = true)
    Share getAnShare(@Param("id") int id);


    @Query(value = """
                    SELECT
                          DISTINCT
                              p.user_id AS owner_id,
                              p.id AS post_id,
                              p.location_id,
                              l.address,
                              a.fullname AS admin_name,
                              a.avatar_url,
                              p.content as post_content,
                              s.content as share_content,
                              p.status,
                              m.type,
                              1 AS isShare,
                              s.create_time,
                              s.user_id AS share_by_user,
                              u.fullname AS share_by_name,
                              u.avatar_url AS share_by_avatar,
                              COUNT(DISTINCT r.id) AS reaction_count,
                              COUNT(DISTINCT c.id) AS comment_count,
                              COUNT(DISTINCT s2.id) AS share_count,
                              COUNT(t.id) AS isTag,
                          MAX(CASE WHEN r.user_id = :userId THEN r.type ELSE NULL END) AS user_reaction_type
                          FROM Share s
                          INNER JOIN Post p ON s.post_id = p.id
                          INNER JOIN Media m ON p.id = m.post_id
                          INNER JOIN Location l ON l.id = p.location_id
                          INNER JOIN User a ON a.id = p.user_id
                          INNER JOIN User u ON u.id = s.user_id
                          LEFT JOIN share_post_reaction r ON s.id = r.share_id
                          LEFT JOIN comment c ON s.id = c.share_id AND c.type = 'SHARE'
                          LEFT JOIN share s2 ON p.id = s2.post_id
                          LEFT JOIN
                              tag t ON t.post_id = p.id
                          WHERE p.status = 'PUBLIC' AND s.id =:shareId
                          GROUP BY p.id, p.user_id, p.content, p.location_id, p.status, m.type, s.create_time, s.user_id
                          ORDER BY create_time DESC
                    """, nativeQuery = true)
    List<Object[]> getAnShareWithID(@Param("shareId") int shareId , @Param("userId") int userId);
}
