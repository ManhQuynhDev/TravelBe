package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Travel_Plan;

public interface TravelPlanRepository extends JpaRepository<Travel_Plan, Integer> {
    @Query(value = "SELECT * FROM Travel_Plan WHERE id = :id AND delflag = 0", nativeQuery = true)
    Travel_Plan getAnTravel_Plan(@Param("id") int id);

    @Query(value = "SELECT * FROM Travel_Plan WHERE group_id = :group_id AND delflag = 0", nativeQuery = true)
    List<Travel_Plan> findByGroupId(@Param("group_id") int group_id);

    @Query(value = "SELECT * FROM Travel_Plan WHERE user_id = :user_id AND delflag = 0", nativeQuery = true)
    List<Travel_Plan> findByGroupUserId(@Param("user_id") int user_id);

    @Query(value = """
                SELECT
            	p.id as planId,
                u.id AS adminId,
                p.plan_name,
                u.fullname AS admin_name,
                p.start_date,
                p.end_date,
                p.description,
                p.status,
                p.create_time,
                SUM(CASE WHEN m.status = 'APPROVED' THEN 1 ELSE 0 END) AS member_count
            FROM
                travel_plan p
            INNER JOIN
                member_plan m ON m.plan_id = p.id
            INNER JOIN
                user u ON p.user_id = u.id
            WHERE
                m.role = "ADMIN" AND p.delflag = 0
            GROUP BY
                p.id, u.id, p.plan_name, p.status, p.description, u.fullname
            ORDER BY
                    ABS(DATEDIFF(CURDATE(), STR_TO_DATE(p.start_date, '%Y/%m/%d'))) ASC;
                                    """, nativeQuery = true)
    Page<Object[]> fetchPlans(Pageable pageable);

    @Query(value = """
                SELECT
                    p.id AS planId,
                    u.id AS adminId,
                    p.plan_name,
                    u.fullname AS admin_name,
                    p.start_date,
                    p.end_date,
                    p.description,
                    p.status,
                    p.create_time,
                    SUM(CASE WHEN m.status = 'APPROVED' THEN 1 ELSE 0 END) AS member_count
                FROM
                    travel_plan p
                INNER JOIN
                    member_plan m ON m.plan_id = p.id
                INNER JOIN
                    user u ON p.user_id = u.id
                WHERE
                    p.group_id = :groupId AND p.delflag = 0
                GROUP BY
                    p.id, u.id, p.plan_name, p.status, p.description, u.fullname, p.start_date, p.create_time, p.end_date
                ORDER BY
                    ABS(DATEDIFF(CURDATE(), STR_TO_DATE(p.start_date, '%Y/%m/%d'))) ASC;
            """, nativeQuery = true)
    Page<Object[]> fetchPlanWithGroupId(@Param("groupId") Integer groupId, Pageable pageable);

}
