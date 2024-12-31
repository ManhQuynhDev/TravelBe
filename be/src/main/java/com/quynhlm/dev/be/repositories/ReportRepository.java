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

       @Query(value = "SELECT * FROM Report WHERE id = :id", nativeQuery = true)
       Report findReportById(Integer id);

       @Query(value = """
                     select 
                     DISTINCT
                     r.id , 
                     u.id as user_report , 
                     r.post_id ,
                     p.user_id as admin_post, 
                     u.fullname , 
                     u.avatar_url , 
                     p.content as contentPost,
                     r.media_url as media_report, 
                     r.reason , 
                     r.violation_type ,
                     r.status,
                     r.action,
                     r.create_time ,
                     r.response_time  
                     from report r
                     INNER JOIN User u on u.id = r.user_id
                     INNER JOIN Post p on p.id = r.post_id
                     where r.user_id = :user_id; """, nativeQuery = true)
       Page<Object[]> getAllReportUserCreate(Integer user_id, Pageable pageable);

       @Query(value = """
                     SELECT  
                     DISTINCT
                     r.id , 
                     u.id as user_report , 
                     r.post_id ,
                     p.user_id as admin_post, 
                     u.fullname , 
                     u.avatar_url , 
                     p.content as contentPost,
                     r.media_url as media_report, 
                     r.reason , 
                     r.violation_type ,
                     r.status,
                     r.action,
                     r.create_time ,
                     r.response_time
                     FROM report r
                     INNER JOIN User u ON u.id = r.user_id
                     INNER JOIN Post p ON p.id = r.post_id
                     GROUP BY r.id, u.id, r.post_id, p.user_id, u.fullname, u.avatar_url, p.content, r.reason, r.violation_type, r.status, r.create_time, r.action, r.response_time
                     """, nativeQuery = true)
       Page<Object[]> getAllReport(Pageable pageable);

       @Query(value = """
                     select 
                     r.id , 
                     u.id as user_report , 
                     r.post_id ,
                     p.user_id as admin_post, 
                     u.fullname , 
                     u.avatar_url , 
                     p.content as contentPost,
                     r.media_url as media_report, 
                     r.reason , 
                     r.violation_type ,
                     r.status,
                     r.action,
                     r.create_time ,
                     r.response_time  from report r
                     INNER JOIN User u on u.id = r.user_id
                     INNER JOIN Post p on p.id = r.post_id
                     where p.delflag = 0 AND r.id = :id;
                     """, nativeQuery = true)
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
