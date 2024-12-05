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
            select r.id ,u.id as user_id , r.post_id , u.fullname , u.avatar_url , p.content as contentPost , m.media_url , m.type, r.reason , r.violation_type ,r.status, r.create_time , r.response_time  from report r
            INNER JOIN User u on u.id = r.user_id
            INNER JOIN Post p on p.id = r.post_id
            INNER JOIN Media m on m.post_id = p.id
            where r.user_id = :user_id;""", nativeQuery = true)
    Page<Object[]> getAllReportUserCreate(Integer user_id, Pageable pageable);


    @Query(value = """
            select r.id ,u.id as user_id , r.post_id , u.fullname , u.avatar_url , p.content as contentPost , m.media_url,m.type, r.reason , r.violation_type ,r.status, r.create_time , r.response_time  from report r
            INNER JOIN User u on u.id = r.user_id
            INNER JOIN Post p on p.id = r.post_id
            INNER JOIN Media m on m.post_id = p.id
            where r.id = :id;""", nativeQuery = true)
    List<Object[]> getAnReport(Integer id);
}
