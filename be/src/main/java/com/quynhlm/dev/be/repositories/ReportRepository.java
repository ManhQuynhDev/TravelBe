package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.quynhlm.dev.be.model.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {
       @Query(value = "SELECT * FROM Report WHERE user_id = :userId AND post_id = :postId", nativeQuery = true)
       Report foundReportExitByUserIdAndPostId(int userId, int postId);

       @Query(value = "SELECT * FROM Report WHERE user_id = :userId AND comment_id = :commentId", nativeQuery = true)
       Report foundReportExitByUserIdAndCommentId(int userId, int commentId);

       @Query(value = "SELECT * FROM Report WHERE id = :id", nativeQuery = true)
       Report findReportById(Integer id);

       @Query(value = """
                         SELECT DISTINCT
                         r.id,
                         u.id AS user_report,
                         COALESCE(r.post_id, r.comment_id) AS related_id,
                         CASE
                             WHEN r.comment_id IS NOT NULL THEN c.user_id
                             ELSE p.user_id
                         END AS admin_related,
                         u.fullname,
                         u.avatar_url,
                         CASE
                             WHEN r.comment_id IS NOT NULL THEN c.content
                             ELSE p.content
                         END AS contentRelated,
                         r.media_url AS media_report,
                         r.reason,
                         r.violation_type,
                         r.status,
                         r.action,
                         r.create_time,
                         r.response_time,
                         CASE
                            WHEN r.comment_id IS NOT NULL THEN "COMMENT"
                            ELSE "POST"
                            END AS type
                     FROM
                         report r
                     INNER JOIN
                         User u ON u.id = r.user_id
                     LEFT JOIN
                         Post p ON p.id = r.post_id
                     LEFT JOIN
                         Comment c ON c.id = r.comment_id
                     WHERE r.user_id = :user_id
                     GROUP BY
                         r.id, u.id, r.post_id, r.comment_id, u.fullname, u.avatar_url,
                         p.content, c.content, r.reason, r.violation_type, r.status,
                         r.create_time, r.action, r.response_time, c.user_id, p.user_id""", nativeQuery = true)
       Page<Object[]> getAllReportUserCreate(Integer user_id, Pageable pageable);

       @Query(value = """
                         SELECT DISTINCT
                         r.id,
                         u.id AS user_report,
                         COALESCE(r.post_id, r.comment_id) AS related_id,
                         CASE
                             WHEN r.comment_id IS NOT NULL THEN c.user_id
                             ELSE p.user_id
                         END AS admin_related,
                         u.fullname,
                         u.avatar_url,
                         CASE
                             WHEN r.comment_id IS NOT NULL THEN c.content
                             ELSE p.content
                         END AS contentRelated,
                         r.media_url AS media_report,
                         r.reason,
                         r.violation_type,
                         r.status,
                         r.action,
                         r.create_time,
                         r.response_time,
                            CASE
                            WHEN r.comment_id IS NOT NULL THEN "COMMENT"
                            ELSE "POST"
                            END AS type
                     FROM
                         report r
                     INNER JOIN
                         User u ON u.id = r.user_id
                     LEFT JOIN
                         Post p ON p.id = r.post_id
                     LEFT JOIN
                         Comment c ON c.id = r.comment_id
                     GROUP BY
                         r.id, u.id, r.post_id, r.comment_id, u.fullname, u.avatar_url,
                         p.content, c.content, r.reason, r.violation_type, r.status,
                         r.create_time, r.action, r.response_time, c.user_id, p.user_id;
                                          """, nativeQuery = true)
       Page<Object[]> getAllReport(Pageable pageable);

       @Query(value = """
                         SELECT DISTINCT
                         r.id,
                         u.id AS user_report,
                         COALESCE(r.post_id, r.comment_id) AS related_id,
                         CASE
                             WHEN r.comment_id IS NOT NULL THEN c.user_id
                             ELSE p.user_id
                         END AS admin_related,
                         u.fullname,
                         u.avatar_url,
                         CASE
                             WHEN r.comment_id IS NOT NULL THEN c.content
                             ELSE p.content
                         END AS contentRelated,
                         r.media_url AS media_report,
                         r.reason,
                         r.violation_type,
                         r.status,
                         r.action,
                         r.create_time,
                         r.response_time,
                         CASE
                            WHEN r.comment_id IS NOT NULL THEN "COMMENT"
                            ELSE "POST"
                            END AS type
                     FROM
                         report r
                     INNER JOIN
                         User u ON u.id = r.user_id
                     LEFT JOIN
                         Post p ON p.id = r.post_id
                     LEFT JOIN
                         Comment c ON c.id = r.comment_id
                     WHERE r.id = :id
                     GROUP BY
                         r.id, u.id, r.post_id, r.comment_id, u.fullname, u.avatar_url,
                         p.content, c.content, r.reason, r.violation_type, r.status,
                         r.create_time, r.action, r.response_time, c.user_id, p.user_id""", nativeQuery = true)
       List<Object[]> getAnReport(Integer id);

       @Query(value = """
                         SELECT v.violation_type,
                                COALESCE(COUNT(r.violation_type), 0) AS count
                         FROM (
                             SELECT 'Spam' AS violation_type
                             UNION ALL
                             SELECT 'Ngôn ngữ thù địch'
                             UNION ALL
                             SELECT 'Quấy rối'
                             UNION ALL
                             SELECT 'Nội dung không phù hợp'
                         ) AS v
                         LEFT JOIN report r ON r.violation_type = v.violation_type
                         GROUP BY v.violation_type
                         UNION ALL
                         SELECT 'Các loại khác' AS violation_type,
                                COUNT(*) AS count
                         FROM report r
                         WHERE r.violation_type NOT IN ('Spam', 'Ngôn ngữ thù địch', 'Quấy rối', 'Nội dung không phù hợp')
                         ORDER BY count DESC;
                     """, nativeQuery = true)
       Page<Object[]> statisticsReport(Pageable pageable);

}
