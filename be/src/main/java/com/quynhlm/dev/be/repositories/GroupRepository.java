package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Integer> {

    @Query(value = "SELECT * FROM m_group WHERE name = :name AND delflag = 0", nativeQuery = true)
    Group findGroupByName(@Param("name") String name);

    @Query(value = "SELECT * FROM m_group WHERE id = :id AND delflag = 0", nativeQuery = true)
    Group findGroupById(@Param("id") Integer id);

    @Query(value = """
            SELECT
                g.id AS groupId,
                u.id AS adminId,
                g.name AS group_name,
                u.fullname AS admin_name,
                g.cover_photo,
                g.bio,
                g.status,
                g.create_time,
                SUM(CASE WHEN m.status = 'APPROVED' THEN 1 ELSE 0 END) AS member_count
            FROM
                m_group g
            INNER JOIN
                member m ON m.group_id = g.id
            INNER JOIN
                user u ON g.user_id = u.id
            WHERE g.id = :group_id AND g.delflag = 0
            GROUP BY
                g.id, u.id, g.name, g.cover_photo, g.status, g.bio, u.fullname, g.create_time
                """, nativeQuery = true)
    List<Object[]> findAnGroupById(@Param("group_id") Integer group_id);

    @Query(value = """
            SELECT
                g.id AS groupId,
                u.id AS adminId,
                g.name AS group_name,
                u.fullname AS admin_name,
                g.cover_photo,
                g.bio,
                g.status,
                g.create_time,
                SUM(CASE WHEN m.status = 'APPROVED' THEN 1 ELSE 0 END) AS member_count
            FROM
                m_group g
            INNER JOIN
                member m ON m.group_id = g.id
            INNER JOIN
                user u ON g.user_id = u.id
            WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :q, '%')) AND g.delflag = 0
            GROUP BY
                g.id, u.id, g.name, g.cover_photo, g.status, g.bio, u.fullname, g.create_time
            """, nativeQuery = true)
    Page<Object[]> searchGroupsByName(@Param("q") String keyword, Pageable pageable);

    Page<Group> findAll(Pageable pageable);

    @Query(value = """
            SELECT
                g.id AS groupId,
                u.id AS adminId,
                g.name AS group_name,
                u.fullname AS admin_name,
                g.cover_photo,
                g.bio,
                g.status,
                g.create_time,
                SUM(CASE WHEN m.status = 'APPROVED' THEN 1 ELSE 0 END) AS member_count
            FROM
                m_group g
            INNER JOIN
                member m ON m.group_id = g.id
            INNER JOIN
                user u ON g.user_id = u.id
            WHERE g.delflag = 0
            GROUP BY
                g.id, u.id, g.name, g.cover_photo, g.status, g.bio, u.fullname, g.create_time
            """, nativeQuery = true)
    Page<Object[]> fetchGroup(Pageable pageable);

    @Query(value = """
                     SELECT
                        g.id AS groupId,
                        u.id AS adminId,
                        g.name AS group_name,
                        u.fullname AS admin_name,
                        g.cover_photo,
                        g.bio,
                        g.status,
                        g.create_time,
                        SUM(CASE WHEN m.status = 'APPROVED' THEN 1 ELSE 0 END) AS member_count,
                        COUNT(t.id) AS travel_plan_count
                    FROM
                        m_group g
                    LEFT JOIN
                        travel_plan t ON g.id = t.group_id
                    INNER JOIN
                        member m ON m.group_id = g.id
                    INNER JOIN
                        user u ON g.user_id = u.id
                    WHERE g.delflag = 0
                    GROUP BY
                        g.id, u.id, g.name, g.cover_photo, g.status, g.bio, u.fullname, g.create_time
                    ORDER BY
                travel_plan_count DESC
            LIMIT 10;
                    """, nativeQuery = true)
    List<Object[]> Top10GroupTravel();

    @Query(value = """
                SELECT
                    g.id AS groupId,
                    u.id AS adminId,
                    g.name AS group_name,
                    u.fullname AS admin_name,
                    g.cover_photo,
                    g.bio,
                    g.status,
                    g.create_time,
                    SUM(CASE WHEN m.status = 'APPROVED' THEN 1 ELSE 0 END) AS member_count,
                    COUNT(t.id) AS travel_plan_count
                FROM
                    m_group g
                LEFT JOIN
                    travel_plan t ON g.id = t.group_id
                INNER JOIN
                    member m ON m.group_id = g.id
                INNER JOIN
                    user u ON g.user_id = u.id
                WHERE g.delflag = 0
                GROUP BY
                    g.id, u.id, g.name, g.cover_photo, g.status, g.bio, u.fullname, g.create_time
                ORDER BY
                    member_count DESC
                LIMIT 10
            """, nativeQuery = true)
    List<Object[]> Top10GroupByMembers();

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
                FROM m_group
                WHERE YEAR(create_time) = :year
            ) p ON m.month = p.month
            GROUP BY m.month
            ORDER BY m.month;
                        """, nativeQuery = true)
    List<Object[]> groupCreateInMonth(@Param("year") Integer year);
}
